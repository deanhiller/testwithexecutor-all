package org.webpieces.execdemo.example.secure;

public class FetchMyInfoResponse {
    private String firstName;
    private String lastName;
    private int coolRating;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getCoolRating() {
        return coolRating;
    }

    public void setCoolRating(int coolRating) {
        this.coolRating = coolRating;
    }
}
