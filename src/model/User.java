package model;

public class User {

    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String country;
    private String gender;
    private String password;

    public User(String fullName, String username, String email,
                String phone, String dateOfBirth, String country,
                String gender, String password) {

        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.country = country;
        this.gender = gender;
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getCountry() {
        return country;
    }

    public String getGender() {
        return gender;
    }

    public String getPassword() {
        return password;
    }
}