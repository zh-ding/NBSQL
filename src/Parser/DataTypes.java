package Parser;

import Exceptions.ParserException;

public class DataTypes {
    int type;
    int int_data;
    long long_data;
    float float_data;
    double double_data;
    String string_data;
    boolean bool_data;

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
        if(data == null)
            this.type = -1;
        else {
            this.type = 4;
            this.string_data = data;
        }
    }

    public DataTypes(boolean data)
    {
        this.type = 5;
        this.bool_data = data;
    }

    public DataTypes add(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(null);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data + b.int_data);
                    case 1:
                        return new DataTypes(this.int_data + b.long_data);
                    case 2:
                        return new DataTypes(this.int_data + b.float_data);
                    case 3:
                        return new DataTypes(this.int_data + b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data + b.int_data);
                    case 1:
                        return new DataTypes(this.long_data + b.long_data);
                    case 2:
                        return new DataTypes(this.long_data + b.float_data);
                    case 3:
                        return new DataTypes(this.long_data + b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data + b.int_data);
                    case 1:
                        return new DataTypes(this.float_data + b.long_data);
                    case 2:
                        return new DataTypes(this.float_data + b.float_data);
                    case 3:
                        return new DataTypes(this.float_data + b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data + b.int_data);
                    case 1:
                        return new DataTypes(this.double_data + b.long_data);
                    case 2:
                        return new DataTypes(this.double_data + b.float_data);
                    case 3:
                        return new DataTypes(this.double_data + b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes minus(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(null);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data - b.int_data);
                    case 1:
                        return new DataTypes(this.int_data - b.long_data);
                    case 2:
                        return new DataTypes(this.int_data - b.float_data);
                    case 3:
                        return new DataTypes(this.int_data - b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data - b.int_data);
                    case 1:
                        return new DataTypes(this.long_data - b.long_data);
                    case 2:
                        return new DataTypes(this.long_data - b.float_data);
                    case 3:
                        return new DataTypes(this.long_data - b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data - b.int_data);
                    case 1:
                        return new DataTypes(this.float_data - b.long_data);
                    case 2:
                        return new DataTypes(this.float_data - b.float_data);
                    case 3:
                        return new DataTypes(this.float_data - b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data - b.int_data);
                    case 1:
                        return new DataTypes(this.double_data - b.long_data);
                    case 2:
                        return new DataTypes(this.double_data - b.float_data);
                    case 3:
                        return new DataTypes(this.double_data - b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes multiply(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(null);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data * b.int_data);
                    case 1:
                        return new DataTypes(this.int_data * b.long_data);
                    case 2:
                        return new DataTypes(this.int_data * b.float_data);
                    case 3:
                        return new DataTypes(this.int_data * b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data * b.int_data);
                    case 1:
                        return new DataTypes(this.long_data * b.long_data);
                    case 2:
                        return new DataTypes(this.long_data * b.float_data);
                    case 3:
                        return new DataTypes(this.long_data * b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data * b.int_data);
                    case 1:
                        return new DataTypes(this.float_data * b.long_data);
                    case 2:
                        return new DataTypes(this.float_data * b.float_data);
                    case 3:
                        return new DataTypes(this.float_data * b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data * b.int_data);
                    case 1:
                        return new DataTypes(this.double_data * b.long_data);
                    case 2:
                        return new DataTypes(this.double_data * b.float_data);
                    case 3:
                        return new DataTypes(this.double_data * b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes divide(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(null);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data / b.int_data);
                    case 1:
                        return new DataTypes(this.int_data / b.long_data);
                    case 2:
                        return new DataTypes(this.int_data / b.float_data);
                    case 3:
                        return new DataTypes(this.int_data / b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data / b.int_data);
                    case 1:
                        return new DataTypes(this.long_data / b.long_data);
                    case 2:
                        return new DataTypes(this.long_data / b.float_data);
                    case 3:
                        return new DataTypes(this.long_data / b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data / b.int_data);
                    case 1:
                        return new DataTypes(this.float_data / b.long_data);
                    case 2:
                        return new DataTypes(this.float_data / b.float_data);
                    case 3:
                        return new DataTypes(this.float_data / b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data / b.int_data);
                    case 1:
                        return new DataTypes(this.double_data / b.long_data);
                    case 2:
                        return new DataTypes(this.double_data / b.float_data);
                    case 3:
                        return new DataTypes(this.double_data / b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes mod(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(null);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data % b.int_data);
                    case 1:
                        return new DataTypes(this.int_data % b.long_data);
                    case 2:
                        return new DataTypes(this.int_data % b.float_data);
                    case 3:
                        return new DataTypes(this.int_data % b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }
            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data % b.int_data);
                    case 1:
                        return new DataTypes(this.long_data % b.long_data);
                    case 2:
                        return new DataTypes(this.long_data % b.float_data);
                    case 3:
                        return new DataTypes(this.long_data % b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data % b.int_data);
                    case 1:
                        return new DataTypes(this.float_data % b.long_data);
                    case 2:
                        return new DataTypes(this.float_data % b.float_data);
                    case 3:
                        return new DataTypes(this.float_data % b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data % b.int_data);
                    case 1:
                        return new DataTypes(this.double_data % b.long_data);
                    case 2:
                        return new DataTypes(this.double_data % b.float_data);
                    case 3:
                        return new DataTypes(this.double_data % b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes notEqual(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(false);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data != b.int_data);
                    case 1:
                        return new DataTypes(this.int_data != b.long_data);
                    case 2:
                        return new DataTypes(this.int_data != b.float_data);
                    case 3:
                        return new DataTypes(this.int_data != b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data != b.int_data);
                    case 1:
                        return new DataTypes(this.long_data != b.long_data);
                    case 2:
                        return new DataTypes(this.long_data != b.float_data);
                    case 3:
                        return new DataTypes(this.long_data != b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data != b.int_data);
                    case 1:
                        return new DataTypes(this.float_data != b.long_data);
                    case 2:
                        return new DataTypes(this.float_data != b.float_data);
                    case 3:
                        return new DataTypes(this.float_data != b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data != b.int_data);
                    case 1:
                        return new DataTypes(this.double_data != b.long_data);
                    case 2:
                        return new DataTypes(this.double_data != b.float_data);
                    case 3:
                        return new DataTypes(this.double_data != b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }
            case 4:
                switch (b.type)
                {
                    case 4:
                        return new DataTypes(!this.string_data.equals(b.string_data));
                    default:
                        throw new ParserException("invalid expression");
                }
            case 5:
                switch (b.type)
                {
                    case 5:
                        return new DataTypes(this.bool_data != b.bool_data);
                    default:
                        throw new ParserException("invalid expression");
                }
            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes equal(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(false);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data == b.int_data);
                    case 1:
                        return new DataTypes(this.int_data == b.long_data);
                    case 2:
                        return new DataTypes(this.int_data == b.float_data);
                    case 3:
                        return new DataTypes(this.int_data == b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data == b.int_data);
                    case 1:
                        return new DataTypes(this.long_data == b.long_data);
                    case 2:
                        return new DataTypes(this.long_data == b.float_data);
                    case 3:
                        return new DataTypes(this.long_data == b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data == b.int_data);
                    case 1:
                        return new DataTypes(this.float_data == b.long_data);
                    case 2:
                        return new DataTypes(this.float_data == b.float_data);
                    case 3:
                        return new DataTypes(this.float_data == b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data == b.int_data);
                    case 1:
                        return new DataTypes(this.double_data == b.long_data);
                    case 2:
                        return new DataTypes(this.double_data == b.float_data);
                    case 3:
                        return new DataTypes(this.double_data == b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }
            case 4:
                switch (b.type)
                {
                    case 4:
                        return new DataTypes(this.string_data.equals(b.string_data));
                    default:
                        throw new ParserException("invalid expression");
                }
            case 5:
                switch (b.type)
                {
                    case 5:
                        return new DataTypes(this.bool_data == b.bool_data);
                    default:
                        throw new ParserException("invalid expression");
                }
            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes greaterEqual(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(false);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data >= b.int_data);
                    case 1:
                        return new DataTypes(this.int_data >= b.long_data);
                    case 2:
                        return new DataTypes(this.int_data >= b.float_data);
                    case 3:
                        return new DataTypes(this.int_data >= b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data >= b.int_data);
                    case 1:
                        return new DataTypes(this.long_data >= b.long_data);
                    case 2:
                        return new DataTypes(this.long_data >= b.float_data);
                    case 3:
                        return new DataTypes(this.long_data >= b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data >= b.int_data);
                    case 1:
                        return new DataTypes(this.float_data >= b.long_data);
                    case 2:
                        return new DataTypes(this.float_data >= b.float_data);
                    case 3:
                        return new DataTypes(this.float_data >= b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data >= b.int_data);
                    case 1:
                        return new DataTypes(this.double_data >= b.long_data);
                    case 2:
                        return new DataTypes(this.double_data >= b.float_data);
                    case 3:
                        return new DataTypes(this.double_data >= b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }
            case 4:
                switch (b.type)
                {
                    case 4:
                        return new DataTypes(this.string_data.compareTo(b.string_data)>=0);
                    default:
                        throw new ParserException("invalid expression");
                }
            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes greater(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(false);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data > b.int_data);
                    case 1:
                        return new DataTypes(this.int_data > b.long_data);
                    case 2:
                        return new DataTypes(this.int_data > b.float_data);
                    case 3:
                        return new DataTypes(this.int_data > b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data > b.int_data);
                    case 1:
                        return new DataTypes(this.long_data > b.long_data);
                    case 2:
                        return new DataTypes(this.long_data > b.float_data);
                    case 3:
                        return new DataTypes(this.long_data > b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data > b.int_data);
                    case 1:
                        return new DataTypes(this.float_data > b.long_data);
                    case 2:
                        return new DataTypes(this.float_data > b.float_data);
                    case 3:
                        return new DataTypes(this.float_data > b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data > b.int_data);
                    case 1:
                        return new DataTypes(this.double_data > b.long_data);
                    case 2:
                        return new DataTypes(this.double_data > b.float_data);
                    case 3:
                        return new DataTypes(this.double_data > b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }
            case 4:
                switch (b.type)
                {
                    case 4:
                        return new DataTypes(this.string_data.compareTo(b.string_data)>0);
                    default:
                        throw new ParserException("invalid expression");
                }
            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes lower(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(false);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data < b.int_data);
                    case 1:
                        return new DataTypes(this.int_data < b.long_data);
                    case 2:
                        return new DataTypes(this.int_data < b.float_data);
                    case 3:
                        return new DataTypes(this.int_data < b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data < b.int_data);
                    case 1:
                        return new DataTypes(this.long_data < b.long_data);
                    case 2:
                        return new DataTypes(this.long_data < b.float_data);
                    case 3:
                        return new DataTypes(this.long_data < b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data < b.int_data);
                    case 1:
                        return new DataTypes(this.float_data < b.long_data);
                    case 2:
                        return new DataTypes(this.float_data < b.float_data);
                    case 3:
                        return new DataTypes(this.float_data < b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data < b.int_data);
                    case 1:
                        return new DataTypes(this.double_data < b.long_data);
                    case 2:
                        return new DataTypes(this.double_data < b.float_data);
                    case 3:
                        return new DataTypes(this.double_data < b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }
            case 4:
                switch (b.type)
                {
                    case 4:
                        return new DataTypes(this.string_data.compareTo(b.string_data)<0);
                    default:
                        throw new ParserException("invalid expression");
                }
            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes lowerEqual(DataTypes b) throws ParserException
    {
        if(this.type == -1 || b.type == -1)
            return new DataTypes(false);
        switch(this.type)
        {
            case 0:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.int_data <= b.int_data);
                    case 1:
                        return new DataTypes(this.int_data <= b.long_data);
                    case 2:
                        return new DataTypes(this.int_data <= b.float_data);
                    case 3:
                        return new DataTypes(this.int_data <= b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 1:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.long_data <= b.int_data);
                    case 1:
                        return new DataTypes(this.long_data <= b.long_data);
                    case 2:
                        return new DataTypes(this.long_data <= b.float_data);
                    case 3:
                        return new DataTypes(this.long_data <= b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 2:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.float_data <= b.int_data);
                    case 1:
                        return new DataTypes(this.float_data <= b.long_data);
                    case 2:
                        return new DataTypes(this.float_data <= b.float_data);
                    case 3:
                        return new DataTypes(this.float_data <= b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }

            case 3:
                switch (b.type)
                {
                    case 0:
                        return new DataTypes(this.double_data <= b.int_data);
                    case 1:
                        return new DataTypes(this.double_data <= b.long_data);
                    case 2:
                        return new DataTypes(this.double_data <= b.float_data);
                    case 3:
                        return new DataTypes(this.double_data <= b.double_data);
                    default:
                        throw new ParserException("invalid expression");
                }
            case 4:
                switch (b.type)
                {
                    case 4:
                        return new DataTypes(this.string_data.compareTo(b.string_data)<=0);
                    default:
                        throw new ParserException("invalid expression");
                }
            default:
                throw new ParserException("invalid expression");
        }
    }

    public Void neg() throws ParserException
    {
        switch(this.type)
        {
            case -1:
                break;
            case 0:
                this.int_data = -this.int_data;
                break;
            case 1:
                this.long_data = -this.long_data;
                break;
            case 2:
                this.float_data = -this.float_data;
                break;
            case 3:
                this.double_data = -this.double_data;
                break;
            case 5:
                this.bool_data = !this.bool_data;
                break;
            default:
                throw new ParserException("invalid expression");
        }
        return null;
    }

    public Void not()
    {
        this.bool_data = !this.bool_data;
        return null;
    }

    public DataTypes and(DataTypes b) throws ParserException
    {
        switch(this.type)
        {
            case 5:
            switch(b.type) {
                case 5:
                    this.bool_data = this.bool_data && b.bool_data;
                    break;
                default:
                    throw new ParserException("invalid expression");
            }
            default:
                throw new ParserException("invalid expression");
        }
    }

    public DataTypes or(DataTypes b) throws ParserException
    {
        switch(this.type)
        {
            case 5:
                switch(b.type) {
                    case 5:
                        this.bool_data = this.bool_data || b.bool_data;
                        break;
                    default:
                        throw new ParserException("invalid expression");
                }
            default:
                throw new ParserException("invalid expression");
        }
    }
}
