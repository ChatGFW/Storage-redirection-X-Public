#!/system/bin/sh

SRX_DATA_DIR="/data/adb/storage-redirect-x"
SRX_PERSISTENT_LOGS="$SRX_DATA_DIR/logs"

# 整体移动目录，SQLite 主库、WAL 与正在打开的文件描述符保持在同一组文件上。
prepare_persistent_logs() {
  active_dir="$1"
  target_dir="$2"
  mkdir -p "$SRX_DATA_DIR" || return 1
  chmod 700 "$SRX_DATA_DIR"
  if [ -d "$active_dir/logs" ] && [ ! -L "$active_dir/logs" ]; then
    if [ -e "$SRX_PERSISTENT_LOGS" ]; then
      mv "$SRX_PERSISTENT_LOGS" "$SRX_DATA_DIR/logs.previous.$(date +%s).$$" || return 1
    fi
    mv "$active_dir/logs" "$SRX_PERSISTENT_LOGS" || return 1
    ln -s "$SRX_PERSISTENT_LOGS" "$active_dir/logs" || return 1
  fi
  mkdir -p "$SRX_PERSISTENT_LOGS" || return 1
  if [ "$target_dir" != "$active_dir" ] || [ ! -L "$target_dir/logs" ]; then
    if [ -e "$target_dir/logs" ]; then
      mv "$target_dir/logs" "$target_dir/logs.before-persistence.$(date +%s).$$" || return 1
    fi
    ln -s "$SRX_PERSISTENT_LOGS" "$target_dir/logs" || return 1
  fi
}
