package com.conk.member.command.application.dto.response;

public final class MemberResponses {
    private MemberResponses() {
    }

    public static class LoginResponse extends com.conk.member.command.application.dto.response.LoginResponse {}
    public static class SetupPasswordResponse extends com.conk.member.command.application.dto.response.SetupPasswordResponse {}
    public static class InviteAccountResponse extends com.conk.member.command.application.dto.response.InviteAccountResponse {}
    public static class SimpleUserStatusResponse extends com.conk.member.command.application.dto.response.SimpleUserStatusResponse {}
    public static class CreateDirectUserResponse extends com.conk.member.command.application.dto.response.CreateDirectUserResponse {}
    public static class CreateCompanyResponse extends com.conk.member.command.application.dto.response.CreateCompanyResponse {}
    public static class CreateAdminUserResponse extends com.conk.member.command.application.dto.response.CreateAdminUserResponse {}
    public static class UpdateAdminUserResponse extends com.conk.member.command.application.dto.response.UpdateAdminUserResponse {}
    public static class CreateSellerResponse extends com.conk.member.command.application.dto.response.CreateSellerResponse {}
    public static class RolePermissionUpdateResponse extends com.conk.member.command.application.dto.response.RolePermissionUpdateResponse {}
    public static class UpdateCompanyResponse extends com.conk.member.command.application.dto.response.UpdateCompanyResponse {}
}
