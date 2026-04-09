package com.example.chatWeb.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1000, "Uncategorized error key", HttpStatus.BAD_REQUEST),

    // Auth & User
    UNAUTHENTICATED(2001, "Xác thực thất bại, vui lòng đăng nhập lại.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2002, "Bạn không có quyền thực hiện hành động này.", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND(2003, "Người dùng không tồn tại.", HttpStatus.NOT_FOUND),
    USER_EXISTED(2004, "Tên người dùng đã tồn tại.", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(2005, "Email đã tồn tại.", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(2007, "Mật khẩu phải có ít nhất {min} ký tự.", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(2008, "Email không hợp lệ.", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(2009,"Username không hợp lệ", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(2010,"Password không hợp lệ", HttpStatus.BAD_REQUEST),

    // Chat & Conversation
    CONVERSATION_NOT_FOUND(3001, "Cuộc hội thoại không tồn tại.", HttpStatus.NOT_FOUND),
    NOT_A_MEMBER(3002, "Bạn không phải là thành viên của cuộc hội thoại này.", HttpStatus.FORBIDDEN),
    ALREADY_A_MEMBER(3003, "Người dùng đã là thành viên của nhóm.", HttpStatus.BAD_REQUEST),
    ONLY_ADMIN_CAN_ACTION(3004, "Chỉ Quản trị viên mới có quyền thực hiện hành động này.", HttpStatus.FORBIDDEN),
    PRIVATE_CHAT_LIMIT(3005, "Chat cá nhân chỉ cho phép tối đa 2 người.", HttpStatus.BAD_REQUEST),

    // Message & Delivery
    MESSAGE_NOT_FOUND(4001, "Tin nhắn không tồn tại.", HttpStatus.NOT_FOUND),
    MESSAGE_SENDER_MISMATCH(4002, "Bạn chỉ có thể chỉnh sửa/xóa tin nhắn của chính mình.", HttpStatus.BAD_REQUEST),
    REPLY_TO_NOT_FOUND(4003, "Tin nhắn trả lời không tồn tại.", HttpStatus.NOT_FOUND),

    // Attachments & Files
    INVALID_FILE_FORMAT(5001, "Định dạng file không hỗ trợ.", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(5002, "Kích thước file vượt quá giới hạn.", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(5003, "Tải file lên thất bại.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
