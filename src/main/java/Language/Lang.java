package Language;

public enum Lang {
    UZ("uz"),
    ES("es"),
    EN("en");


    private String name1;

    Lang(String name1){
        this.name1 = name1;
    }

    public String getName1() {
        return name1;
    }
}