# Kinh nghiệm xử lý lỗi MCP Proxy Socket Connection ENOENT trong Antigravity IDE

## 1. Hiện tượng
- Xuất hiện thông báo lỗi khởi tạo MCP khi mở Antigravity IDE:
```
data-agent-kit: [MCP Proxy] Socket connection error: connect ENOENT \\?\pipe\datacloud-mcp-dataAgentKit-antigravityide : connection closed: calling "initialize": client is closing: EOF
notebooks: [MCP Proxy] Socket connection error: connect ENOENT \\?\pipe\datacloud-mcp-notebooks-antigravityide : connection closed: calling "initialize": client is closing: EOF
visualization: [MCP Proxy] Socket connection error: connect ENOENT \\?\pipe\datacloud-mcp-visualization-antigravityide : connection closed: calling "initialize": client is closing: EOF
```

## 2. Nguyên nhân kỹ thuật
- File cấu hình MCP tại `C:\Users\Admin\.gemini\config\mcp_config.json` khai báo 3 server: `notebooks`, `visualization`, `data-agent-kit`.
- Cả 3 đều chạy qua proxy script: `.../googlecloudtools.datacloud-1.1.1-universal/mcp_servers/cli/mcp_proxy_bundle.js`.
- Script này hoạt động như một Unix Socket / Windows Named Pipe proxy (`\\?\pipe\datacloud-mcp-<service>`), kết nối đến backend service do Extension **Google Cloud Data Cloud** của IDE quản lý.
- Lỗi `ENOENT` xảy ra do **Race Condition lúc khởi động IDE**: MCP process cố gắng kết nối trong khi Extension Google Data Cloud chưa kịp khởi động và tạo Named Pipe (mặc định retry 10 lần x 2s = 20s). Sau khi hết thời gian chờ, proxy thoát với mã lỗi EOF.

## 3. Cách kiểm tra & khắc phục
1. **Kiểm tra trạng thái Pipe**:
   ```powershell
   [System.IO.Directory]::GetFiles("\\.\pipe\") | Where-Object { $_ -match "datacloud|mcp" }
   ```
2. **Nếu cần dùng GCP MCPs**:
   - Chờ Extension kích hoạt xong rồi Reload IDE Window (`Ctrl+Shift+P` -> `Reload Window`).
3. **Nếu không sử dụng GCP MCPs**:
   - Xóa hoặc comment các server `notebooks`, `visualization`, `data-agent-kit` trong `C:\Users\Admin\.gemini\config\mcp_config.json` để tránh IDE thử khởi động và ghi log lỗi phiền toái.
