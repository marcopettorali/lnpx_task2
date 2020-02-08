package lnpx;

class lastTweetId {

    private String user;
    private Long Id;

    public lastTweetId(String user, Long Id) {
        this.user = user;
        this.Id = Id;
    }

    public String getUser() {
        return user;
    }

    public Long getId() {
        return Id;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setId(Long Id) {
        this.Id = Id;
    }

    @Override
    public String toString() {
        return user + ":" + Id;
    }

}
