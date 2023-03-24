package tanks;

import java.util.HashMap;

public class CustomPropertiesMap extends HashMap<String, Object>
{
    public Integer getInt(String key)
    {
        Object o = get(key);
        if (o == null)
            return null;

        return (Integer) o;
    }

    public Double getDouble(String key)
    {
        Object o = get(key);
        if (o == null)
            return null;

        return (Double) o;
    }

    public Long getLong(String key)
    {
        Object o = get(key);
        if (o == null)
            return null;

        return (Long) o;
    }

    public Float getFloat(String key)
    {
        Object o = get(key);
        if (o == null)
            return null;

        return (Float) o;
    }

    public String getString(String key)
    {
        Object o = get(key);
        if (o == null)
            return null;

        return (String) o;
    }
}
