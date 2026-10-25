use libc::{O_APPEND, O_CREAT, O_RDWR, O_TMPFILE, O_TRUNC, O_WRONLY};

pub fn has_write_intent_flags(flags: i32) -> bool {
    if flags < 0 {
        return false;
    }

    let write_mask = O_WRONLY | O_RDWR | O_CREAT | O_TRUNC | O_APPEND;
    (flags & write_mask) != 0 || (flags & O_TMPFILE) == O_TMPFILE
}
