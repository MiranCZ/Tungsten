package me.miran;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Util {
    private Util() {
    }

    public static final HashMap<Item, Block> itemIntoBlockMap;
    static {
        itemIntoBlockMap = new HashMap<>();

        List<Field> staticStringFields = getPublicStaticFieldsOfType(Blocks.class, Block.class);


        for (Field field : staticStringFields) {

            Block b;
            try {
                b = (Block) field.get(Blocks.class);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            itemIntoBlockMap.put(b.asItem(),b);
        }
    }

    public static <T> List<Field> getPublicStaticFieldsOfType(Class<?> clazz, Class<T> fieldType) {
        List<Field> fieldsOfType = new ArrayList<>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && fieldType.isAssignableFrom(field.getType())) {
                fieldsOfType.add(field);
            }
        }

        return fieldsOfType;
    }
}
