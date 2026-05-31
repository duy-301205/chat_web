# 💬 Real-time Chat Web Application - Backend

Hệ thống Backend được xây dựng bằng **Java Spring Boot**, cung cấp giải pháp nhắn tin thời gian thực (Real-time) bảo mật, hiệu năng cao, hỗ trợ cả trò chuyện cá nhân (1-1) và trò chuyện nhóm.

---

## 🚀 Tính năng cốt lõi (Core Features)

* **Real-time Messaging:** Giao tiếp hai chiều độ trễ cực thấp sử dụng kết nối **WebSocket**.
* **Authentication & Authorization:** Xác thực người dùng và bảo mật hệ thống toàn diện với **Spring Security** và **JWT (JSON Web Token)**.
* **Message Delivery Status:** Cập nhật trạng thái tin nhắn theo thời gian thực (Gửi thành công -> Đã nhận -> Đã xem).
* **Real-time Tracking:** Theo dõi trạng thái hoạt động của người dùng (Online/Offline Status) và đếm số tin nhắn chưa đọc (Unread Count).
* **Conversation Management:** API quản lý thông tin người dùng, danh sách bạn bè, tạo phòng chat nhóm và lưu trữ lịch sử tin nhắn.

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

* **Framework chính:** Spring Boot (Spring Web, Spring Security, Spring WebSocket)
* **Data Access:** Hibernate / Spring Data JPA
* **Cơ sở dữ liệu chính:** PostgreSQL (Lưu trữ thông tin người dùng, phòng chat, lịch sử tin nhắn)
* **Bộ nhớ đệm & Real-time State:** Redis (In-memory Database dùng quản lý phiên WebSocket và bộ đếm trạng thái hoạt động)
* **Công cụ khác:** Git, Maven

---

## 📐 Kiến trúc hệ thống & Luồng xử lý (Architecture & Workflow)

1. **Authentication Flow:** Người dùng đăng nhập qua REST API -> Hệ thống cấp mã JWT -> Các request tiếp theo hoặc yêu cầu kết nối WebSocket bắt buộc phải đính kèm JWT này để xác thực.
2. **WebSocket Handshake:** Khi kết nối thiết lập, hệ thống thực hiện xác thực (Handshake), nếu hợp lệ sẽ tạo một Session hoạt động và lưu trạng thái `Online` của người dùng vào **Redis**.
3. **Data Flow:** Tin nhắn từ client gửi lên WebSocket Handler -> Hệ thống lưu song song vào **PostgreSQL** để lưu lịch sử, đồng thời chuyển tiếp (broadcast) ngay lập tức tới các client đang kết nối trong phòng chat.
