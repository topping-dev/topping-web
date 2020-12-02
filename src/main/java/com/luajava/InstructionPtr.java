package com.luajava;

/*
** $Id: lcode.c,v 2.25.1.3 2007/12/28 15:32:23 roberto Exp $
** Code generator for Lua
** See Copyright Notice in lua.h
*/


//FUCK NOTE: There is no Java equivalent to C# namespace aliases:
//using TValue = Lua.lua_TValue;
//FUCK NOTE: There is no Java equivalent to C# namespace aliases:
//using lua_Number = System.Double;
//FUCK NOTE: There is no Java equivalent to C# namespace aliases:
//using Instruction = System.UInt32;

public class InstructionPtr
{
//FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public System.UInt32[] codes;
    public Long[] codes;
    public int pc;

    public InstructionPtr()
    {
        this.codes = null;
        ;
        this.pc = -1;
    }
//FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public InstructionPtr(System.UInt32[] codes, int pc)
    public InstructionPtr(Long[] codes, int pc)
    {
        this.codes = codes;
        this.pc = pc;
    }
    public static InstructionPtr Assign(InstructionPtr ptr)
    {
        if (ptr == null)
        {
            return null;
        }
        return new InstructionPtr(ptr.codes, ptr.pc);
    }
//FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public System.UInt32 getItem(int index)
    public final long getItem(int index)
    {
        return this.codes[pc + index];
    }
//FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void setItem(int index, System.UInt32 value)
    public final void setItem(int index, long value)
    {
        this.codes[pc + index] = value;
    }
    public static InstructionPtr inc(RefObject<InstructionPtr> ptr)
    {
        InstructionPtr result = new InstructionPtr(ptr.argvalue.codes, ptr.argvalue.pc);
        ptr.argvalue.pc++;
        return result;
    }
    public static InstructionPtr dec(RefObject<InstructionPtr> ptr)
    {
        InstructionPtr result = new InstructionPtr(ptr.argvalue.codes, ptr.argvalue.pc);
        ptr.argvalue.pc--;
        return result;
    }
    public static boolean OpLessThan(InstructionPtr p1, InstructionPtr p2)
    {
        assert p1.codes == p2.codes;
        return p1.pc < p2.pc;
    }
    public static boolean OpGreaterThan(InstructionPtr p1, InstructionPtr p2)
    {
        assert p1.codes == p2.codes;
        return p1.pc > p2.pc;
    }
    public static boolean OpLessThanOrEqual(InstructionPtr p1, InstructionPtr p2)
    {
        assert p1.codes == p2.codes;
        return p1.pc < p2.pc;
    }
    public static boolean OpGreaterThanOrEqual(InstructionPtr p1, InstructionPtr p2)
    {
        assert p1.codes == p2.codes;
        return p1.pc > p2.pc;
    }
}