package com.conk.member.command.application.dto.request;

public final class MemberRequests {
    private MemberRequests() {
    }

    public static class LoginRequest extends com.conk.member.command.application.dto.request.LoginRequest {}
    public static class SetupPasswordRequest extends com.conk.member.command.application.dto.request.SetupPasswordRequest {}
    public static class InviteAccountRequest extends com.conk.member.command.application.dto.request.InviteAccountRequest {}
    public static class CreateDirectUserRequest extends com.conk.member.command.application.dto.request.CreateDirectUserRequest {}
    public static class CreateCompanyRequest extends com.conk.member.command.application.dto.request.CreateCompanyRequest {}
    public static class CreateAdminUserRequest extends com.conk.member.command.application.dto.request.CreateAdminUserRequest {}
    public static class UpdateAdminUserRequest extends com.conk.member.command.application.dto.request.UpdateAdminUserRequest {}
    public static class CreateSellerRequest extends com.conk.member.command.application.dto.request.CreateSellerRequest {}
    public static class UpdateCompanyRequest extends com.conk.member.command.application.dto.request.UpdateCompanyRequest {}
    public static class UpdateRolePermissionsRequest extends com.conk.member.command.application.dto.request.UpdateRolePermissionsRequest {}
}
