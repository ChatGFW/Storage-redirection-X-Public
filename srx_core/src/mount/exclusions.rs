use super::rules::{REAL_PATH_WILDCARD_EXPAND_LIMIT, has_path_wildcard, reduce_real_path_rules};
use super::{MountPlanner, PrimaryMountFailure};
use crate::platform::paths;

impl MountPlanner {
    pub(super) fn apply_excluded_real_paths(
        &self,
        excluded_rules: &[String],
        restored_real_paths: &[String],
        storage_path: &str,
        redirect_backend: &str,
    ) -> bool {
        let mut excluded_paths = Vec::new();
        let storage_prefix = format!("{}/", storage_path);
        for rule in excluded_rules {
            let resolved =
                self.resolve_user_path(&self.resolve_placeholders(&self.normalize_path(rule)));
            if !resolved.starts_with(&storage_prefix) || paths::has_unsafe_segments(&resolved) {
                continue;
            }
            if has_path_wildcard(&resolved) {
                let expanded =
                    Self::expand_real_path_wildcard(&resolved, storage_path, storage_path);
                if expanded.len() >= REAL_PATH_WILDCARD_EXPAND_LIMIT {
                    log::warn!(
                        "exclude wildcard limit path={} limit={}",
                        resolved,
                        REAL_PATH_WILDCARD_EXPAND_LIMIT
                    );
                }
                excluded_paths.extend(expanded);
            } else {
                excluded_paths.push(resolved);
            }
        }
        // 未被真实目录覆盖的路径已位于沙盒，只处理与恢复挂载重叠的排除项。
        excluded_paths.retain(|excluded| {
            restored_real_paths.iter().any(|restored| {
                paths::matches(restored, excluded, true) || paths::matches(excluded, restored, true)
            })
        });
        excluded_paths.sort();
        excluded_paths.dedup();
        for excluded in reduce_real_path_rules(excluded_paths) {
            let relative = &excluded[storage_prefix.len()..];
            let source = paths::join(redirect_backend, relative);
            if !self.ensure_writable_mapped_directory(&source, self.app_uid)
                || !self.ensure_directory_exists(&excluded, true)
            {
                log::error!("exclude directory prep failed: {}", excluded);
                return false;
            }
            if !self.bind_mount_with_storage_aliases(
                &source,
                &excluded,
                true,
                PrimaryMountFailure::AbortAll,
                Some("exclude mount failed"),
                Some("exclude alias mount failed"),
                Some("exclude alias mount ok"),
                None,
            ) {
                return false;
            }
            log::info!("exclude redirected {}", excluded);
        }
        true
    }
}
