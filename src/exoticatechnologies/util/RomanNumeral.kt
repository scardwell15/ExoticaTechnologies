package exoticatechnologies.util

import java.util.*

object RomanNumeral {
    private val map: TreeMap<Int, String> = TreeMap<Int, String>();

    init {
        map[1000] = "M";
        map[900] = "CM";
        map[500] = "D";
        map[400] = "CD";
        map[100] = "C";
        map[90] = "XC";
        map[50] = "L";
        map[40] = "XL";
        map[10] = "X";
        map[9] = "IX";
        map[5] = "V";
        map[4] = "IV";
        map[1] = "I";
    }

     fun toRoman(number: Int): String
    {
        val l = map.floorKey(number);
        if (number == l) {
            return map[number]!!
        }
        return map[l] + toRoman(number - l)
    }
}