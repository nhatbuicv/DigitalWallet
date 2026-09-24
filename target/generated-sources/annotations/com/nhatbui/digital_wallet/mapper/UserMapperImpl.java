package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.admin.UserAdminResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_wallet.entity.User;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-24T20:18:37+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserProfileResponse mapToUserProfileResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserProfileResponse.UserProfileResponseBuilder userProfileResponse = UserProfileResponse.builder();

        userProfileResponse.uid( user.getUid() );
        userProfileResponse.customerId( user.getCustomerId() );
        userProfileResponse.fullName( user.getFullName() );
        userProfileResponse.email( user.getEmail() );
        userProfileResponse.phone( user.getPhone() );
        userProfileResponse.dateOfBirth( user.getDateOfBirth() );
        userProfileResponse.address( user.getAddress() );
        if ( user.getStatus() != null ) {
            userProfileResponse.status( user.getStatus().name() );
        }
        if ( user.getRole() != null ) {
            userProfileResponse.role( user.getRole().name() );
        }
        userProfileResponse.phoneVerified( user.getPhoneVerified() );
        userProfileResponse.walletNonLocked( user.getWalletNonLocked() );
        userProfileResponse.failedLoginAttempts( user.getFailedLoginAttempts() );
        userProfileResponse.lastLogin( user.getLastLogin() );
        userProfileResponse.createdAt( user.getCreatedAt() );
        userProfileResponse.updatedAt( user.getUpdatedAt() );

        return userProfileResponse.build();
    }

    @Override
    public UserAdminResponse mapToUserAdminResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserAdminResponse.UserAdminResponseBuilder userAdminResponse = UserAdminResponse.builder();

        userAdminResponse.fullName( user.getFullName() );
        userAdminResponse.email( user.getEmail() );
        userAdminResponse.phone( user.getPhone() );
        if ( user.getDateOfBirth() != null ) {
            userAdminResponse.dateOfBirth( DateTimeFormatter.ISO_LOCAL_DATE.format( user.getDateOfBirth() ) );
        }
        userAdminResponse.address( user.getAddress() );
        if ( user.getStatus() != null ) {
            userAdminResponse.status( user.getStatus().name() );
        }
        if ( user.getRole() != null ) {
            userAdminResponse.role( user.getRole().name() );
        }
        userAdminResponse.phoneVerified( user.getPhoneVerified() );
        userAdminResponse.walletNonLocked( user.getWalletNonLocked() );
        userAdminResponse.failedLoginAttempts( user.getFailedLoginAttempts() );
        userAdminResponse.lockTime( user.getLockTime() );
        userAdminResponse.lastLogin( user.getLastLogin() );
        userAdminResponse.createdAt( user.getCreatedAt() );
        userAdminResponse.updatedAt( user.getUpdatedAt() );

        userAdminResponse.uid( user.getUid() != null ? user.getUid().toString() : null );

        return userAdminResponse.build();
    }
}
