package lab4SET;
public class Gracz {
    String nickname;
    int punkty;
    int wiek;

    public Gracz(int wiek, String nickname, int punkty)
    {
        this.wiek = wiek;
        this.nickname = nickname;
        this.punkty = punkty;
    }

    public String toString()
    {
        return  "Gracz{" +
                "nickname='" + nickname + '\'' +
                ", punkty=" + punkty +
                ", wiek=" + wiek +
                "}\n";
    }


}

