package tanks.network;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NetworkUtils 
{
	public static final Charset charset = StandardCharsets.UTF_8;
	
	public static String readString(ByteBuf b)
	{
		int l = b.readInt();

		if (l < 0)
			return null;

		return b.readCharSequence(l, charset).toString();
	}
	
	public static void writeString(ByteBuf b, String s)
	{
		int extra = 0;

		if (s == null)
		{
			b.writeInt(-1);
			return;
		}

		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == '\u00A7')
				extra++;
		
		b.writeInt(s.length() + extra);
		b.writeCharSequence(s, charset);
	}

	public static final Class<?>[] supportedTypes = {Integer.class, Double.class, Boolean.class, String.class, Enum.class, Float.class, Long.class,
			int.class, double.class, boolean.class, float.class, long.class};

	public static void writeFields(ByteBuf b, Object obj, String... fieldNames)
	{
		Field[] fields = new Field[fieldNames.length];
		Class<?> objClass = obj.getClass();

		try
		{
			for (int i = 0; i < fieldNames.length; i++)
				fields[i] = objClass.getField(fieldNames[i]);
		}
		catch (NoSuchFieldException e)
		{
			throw new RuntimeException(e);
		}

		writeFields(b, obj, fields);
	}


	/** May be slow, recommended to use only when calling once */
	public static void writeFields(ByteBuf b, Object obj, Field... fields)
	{
		try
		{
			for (Field f : fields)
			{
				Object val = f.get(obj);

				int i;
				boolean found = false;
				Class<?> cls = val.getClass();

				for (i = 0; i < supportedTypes.length; i++)
				{
					if (supportedTypes[i] == cls)
					{
						found = true;
						break;
					}
				}

				if (!found)
					throw new InvalidTypeException(val.getClass().getSimpleName());

				if (i >= 7)
					i -= 7;

				b.writeInt(i);
				if (i == 0)
					b.writeInt((Integer) val);
				else if (i == 1)
					b.writeDouble((Double) val);
				else if (i == 2)
					b.writeBoolean((Boolean) val);
				else if (i == 3)
					NetworkUtils.writeString(b, ((String) val));
				else if (i == 4)
					b.writeInt(((Enum<?>) val).ordinal());
				else if (i == 5)
					b.writeLong(((Long) val));
				else
					b.writeFloat(((Float) val));
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void readFields(ByteBuf b, Object obj, String... fieldNames)
	{
		Field[] fields = new Field[fieldNames.length];
		Class<?> objClass = obj.getClass();

		try
		{
			for (int i = 0; i < fieldNames.length; i++)
				fields[i] = objClass.getField(fieldNames[i]);
		}
		catch (NoSuchFieldException e)
		{
			throw new RuntimeException(e);
		}

		readFields(b, obj, fields);
	}

	public static void readFields(ByteBuf b, Object obj, Field... fields)
	{
		try
		{
			for (Field f : fields)
			{
				int type = b.readInt();

				if (type == 0)
					f.setInt(obj, b.readInt());
				else if (type == 1)
					f.setDouble(obj, b.readDouble());
				else if (type == 2)
					f.setBoolean(obj, b.readBoolean());
				else if (type == 3)
					f.set(obj, NetworkUtils.readString(b));
				else if (type == 4)
					f.set(obj, f.getType().getEnumConstants()[b.readInt()]);
				else if (type == 5)
					f.setLong(obj, b.readLong());
				else if (type == 6)
					f.setFloat(obj, b.readFloat());
				else
					throw new InvalidTypeException(type + "");
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static final class InvalidTypeException extends Exception
	{
		public InvalidTypeException(String type)
		{
			super(type);
		}
	}
}
