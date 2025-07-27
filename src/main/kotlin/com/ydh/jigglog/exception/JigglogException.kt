package com.ydh.jigglog.exception

// 기본 예외 클래스
sealed class JigglogException(
    message: String,
    val errorCode: String
) : RuntimeException(message)

// 인증 관련 예외
class AuthenticationException(
    message: String = "인증에 실패했습니다",
    errorCode: String = "AUTH_FAILED"
) : JigglogException(message, errorCode)

// 사용자 관련 예외
class UserNotFoundException(
    message: String = "사용자를 찾을 수 없습니다",
    errorCode: String = "USER_NOT_FOUND"
) : JigglogException(message, errorCode)

class DuplicateUsernameException(
    message: String = "이미 존재하는 사용자명입니다",
    errorCode: String = "DUPLICATE_USERNAME"
) : JigglogException(message, errorCode)

class InvalidPasswordException(
    message: String = "비밀번호가 일치하지 않습니다",
    errorCode: String = "INVALID_PASSWORD"
) : JigglogException(message, errorCode)

// 포스트 관련 예외
class PostNotFoundException(
    message: String = "포스트를 찾을 수 없습니다",
    errorCode: String = "POST_NOT_FOUND"
) : JigglogException(message, errorCode)

// 댓글 관련 예외
class CommentNotFoundException(
    message: String = "댓글을 찾을 수 없습니다",
    errorCode: String = "COMMENT_NOT_FOUND"
) : JigglogException(message, errorCode)

// 권한 관련 예외
class UnauthorizedException(
    message: String = "권한이 없습니다",
    errorCode: String = "UNAUTHORIZED"
) : JigglogException(message, errorCode)

// 유효성 검증 예외
class ValidationException(
    message: String,
    errorCode: String = "VALIDATION_ERROR"
) : JigglogException(message, errorCode) 