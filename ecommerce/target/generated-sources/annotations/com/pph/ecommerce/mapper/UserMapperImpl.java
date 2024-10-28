package com.pph.ecommerce.mapper;

import com.pph.ecommerce.dto.request.UserCreationRequest;
import com.pph.ecommerce.dto.response.UserResponse;
import com.pph.ecommerce.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.username( request.getUsername() );
        user.password( request.getPassword() );
        user.firstname( request.getFirstname() );
        user.lastname( request.getLastname() );
        user.email( request.getEmail() );
        user.phoneNumber( request.getPhoneNumber() );
        user.status( request.getStatus() );
        user.dateOfBirth( request.getDateOfBirth() );

        return user.build();
    }

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.username( user.getUsername() );
        userResponse.password( user.getPassword() );
        userResponse.firstname( user.getFirstname() );
        userResponse.lastname( user.getLastname() );
        userResponse.email( user.getEmail() );
        userResponse.phoneNumber( user.getPhoneNumber() );
        userResponse.dateOfBirth( user.getDateOfBirth() );

        return userResponse.build();
    }
}
