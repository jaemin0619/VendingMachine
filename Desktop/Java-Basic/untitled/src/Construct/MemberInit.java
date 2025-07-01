package Construct;

public class MemberInit {
    String name;
    int age;
    int grade;
    //추가
    void initMember(MemberInit member,String name,int age,int grade)
    {
        this.name=name; //this -> 자기 자신의 이름
        this.age=age;
        this.grade=grade;
    }
}

