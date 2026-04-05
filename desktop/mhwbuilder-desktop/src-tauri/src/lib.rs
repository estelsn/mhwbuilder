#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    use tauri_plugin_shell::ShellExt;

    tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())
        .setup(|app| {
            let _ = app
                .shell()
                .sidecar("mhwb-backend")
                .expect("failed to prepare backend sidecar")
                .spawn()
                .expect("failed to start backend sidecar");

            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
