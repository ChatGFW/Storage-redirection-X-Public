use std::io;
use std::time::{Duration, Instant};

pub(super) fn read_result(fd: i32, timeout: Duration) -> io::Result<bool> {
    let deadline = Instant::now() + timeout;
    let mut bytes = [0u8; 4];
    let mut received = 0;
    while received < bytes.len() {
        let remaining = deadline.saturating_duration_since(Instant::now());
        if remaining.is_zero() {
            return Err(io::Error::new(
                io::ErrorKind::TimedOut,
                "mount result timeout",
            ));
        }
        let mut event = libc::pollfd {
            fd,
            events: libc::POLLIN,
            revents: 0,
        };
        // specialize 后无权修改 zygote socket 的选项，按同一截止时间等待可读。
        let wait_ms = remaining.as_millis().clamp(1, i32::MAX as u128) as i32;
        let ready = unsafe { libc::poll(&mut event, 1, wait_ms) };
        if ready == 0 {
            continue;
        }
        if ready < 0 {
            let error = io::Error::last_os_error();
            if error.kind() == io::ErrorKind::Interrupted {
                continue;
            }
            return Err(error);
        }
        let count = unsafe {
            libc::recv(
                fd,
                bytes[received..].as_mut_ptr().cast(),
                bytes.len() - received,
                libc::MSG_DONTWAIT,
            )
        };
        if count == 0 {
            return Err(io::Error::new(
                io::ErrorKind::UnexpectedEof,
                "incomplete mount result",
            ));
        }
        if count < 0 {
            let error = io::Error::last_os_error();
            if matches!(
                error.kind(),
                io::ErrorKind::Interrupted | io::ErrorKind::WouldBlock
            ) {
                continue;
            }
            return Err(error);
        }
        received += count as usize;
    }
    Ok(i32::from_ne_bytes(bytes) == 1)
}
