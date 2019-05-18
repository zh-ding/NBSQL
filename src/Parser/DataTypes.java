package Parser;

public class DataTypes {
    int type;
    int int_data;
    long long_data;
    float float_data;
    double double_data;
    String string_data;

    public DataTypes(int data)
    {
        this.type = 0;
        this.int_data = data;
    }

    public DataTypes(long data)
    {
        this.type = 1;
        this.long_data = data;
    }

    public DataTypes(float data)
    {
        this.type = 2;
        this.float_data = data;
    }

    public DataTypes(double data)
    {
        this.type = 3;
        this.double_data = data;
    }

    public DataTypes(String data)
    {
        this.type = 4;
        this.string_data = data;
    }
}
