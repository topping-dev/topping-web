function isFloat(n) {
    return n === +n && n !== (n|0);
}

function isInteger(n) {
    return n === +n && n === (n|0);
}

const TYPE_NULL = 0;
const TYPE_INTEGER = 1;
const TYPE_FLOAT = 2;
const TYPE_STRING = 3;
const TYPE_BOOLEAN = 4;
const TYPE_OBJECT = 5;

function readVariable(buf)
{
    var type = buf.readInt();
    if(type == TYPE_NULL)
        return null;
    else if(type == TYPE_INTEGER)
        return buf.readInt();
    else if(type == TYPE_FLOAT)
        return buf.readFloat();
    else if(type == TYPE_STRING)
    {
        var length = buf.readInt();
        return buf.readUTF8String(length);
    }
    else if(type == TYPE_BOOLEAN)
        return buf.readInt() == 1;
    else if(type == TYPE_OBJECT)
    {
        var length = buf.readInt();
        var objStr = buf.readUTF8String(length);
        return JSON.parse(objStr);
    }

    return null;
}

function writeVariable(buf, val)
{
    if(val == null)
    {
        buf.writeInt(TYPE_NULL);
        return buf;
    }
    if(typeof val === "number")
    {
        if(isInteger(val))
        {
            buf.writeInt(TYPE_INTEGER);
            buf.writeInt(val);
        }
        else
        {
            buf.writeInt(TYPE_FLOAT);
            buf.writeFloat(val);
        }
    }
    else if(typeof val === "string")
    {
        buf.writeInt(TYPE_STRING);
        buf.writeInt(val.length);
        buf.writeString(val);
    }
    else if(typeof val === "boolean")
    {
        buf.writeInt(TYPE_BOOLEAN);
        buf.writeInt(val ? 1 : 0);
    }
    else if(typeof val === "object")
    {
        buf.writeInt(TYPE_OBJECT);
        var valStr = JSON.stringify(val);
        buf.writeInt(valStr.length);
        buf.writeString(valStr);
    }

    return buf;
}