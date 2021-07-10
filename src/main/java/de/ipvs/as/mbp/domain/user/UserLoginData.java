package de.ipvs.as.mbp.domain.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Wrapper for user login data that is passed with an user authentication request.
 */
@ApiModel(description = "Login data for user authentication requests")
public class UserLoginData {
    @ApiModelProperty(notes = "Username of the user to authenticate", example = "MyUser", required = true)
    private String username;
    @ApiModelProperty(notes = "Password of the user to authenticate", example = "secret", required = true)
    private String password;

    /**
     * Creates a new empty user auth data object.
     */
    public UserLoginData() {

    }

    /**
     * Creates a new user auth data object from a given username and password.
     *
     * @param username The user name to use
     * @param password The password to use
     */
    public UserLoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the username that is part of the auth data.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the auth data.
     *
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password that is part of the auth data.
     *
     * @return The The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the auth data.
     *
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
