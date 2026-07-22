class ExampleModel {
    private String username;
    private String password;



    int a;
    long b;
    short c;
    boolean d;
    double e;
    float f;
    char g;
    byte h;

    public void setUsername(String u) throws Exception {
        //we can do other stuff before we let this happen
        if(u.length() < 3) {
            throw new Exception("Minimum 3 chars");
        }
        this.username = u;
        return;
    }

    public String getUsername() {
        return this.username;
    }
}
