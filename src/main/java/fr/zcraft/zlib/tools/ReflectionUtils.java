/*
 * Copyright or © or Copr. ZLib contributors (2015)
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.zcraft.zlib.tools;

import fr.zcraft.zlib.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * A set of tools to simplify reflective operations on Bukkit and the Native Minecraft Server.
 *
 * @author ProkopyL
 * @author Amaury Carrade (documentation only).
 */
abstract public class ReflectionUtils
{
    /**
     * Returns the Bukkit's current version, as read in the Bukkit's package name.
     *
     * @return The Bukkit's version in the package name.
     */
    static public String getBukkitPackageVersion()
    {
        return getBukkitPackageName().substring("org.bukkit.craftbukkit.".length());
    }

    /**
     * Returns the full name of the root Bukkit package: something like
     * "org.bukkit.craftbukkit.v1_8_R3".
     *
     * @return the full name of the root Bukkit package.
     */
    static public String getBukkitPackageName()
    {
        return Bukkit.getServer().getClass().getPackage().getName();
    }

    /**
     * Returns the full name of the root NMS package: something like "net.minecraft.server.v1_8_R3".
     *
     * @return the full name of the root NMS package.
     */
    static public String getMinecraftPackageName()
    {
        return "net.minecraft.server." + getBukkitPackageVersion();
    }

    /**
     * Returns the {@link Class} of a Bukkit class from it's name (without the main Bukkit
     * package).
     *
     * As example, with "Server", this method returns the {@code org.bukkit.craftbukkit.v1_X_RX.Server}
     * class.
     *
     * @param name The Bukkit's class name (without the main Bukkit package).
     *
     * @return The class.
     * @throws ClassNotFoundException if no class exists with this name in the Bukkit package.
     */
    static public Class getBukkitClassByName(String name) throws ClassNotFoundException
    {
        return Class.forName(getBukkitPackageName() + "." + name);
    }

    /**
     * Returns the {@link Class} of a NMS class from it's name (without the main NMS package).
     *
     * As example, with "Server", this method returns the {@code net.minecraft.server.v1_X_RX.Server}
     * class.
     *
     * @param name The NMS' class name (without the main Bukkit package).
     *
     * @return The class.
     * @throws ClassNotFoundException if no class exists with this name in the NMS package.
     */
    static public Class getMinecraftClassByName(String name) throws ClassNotFoundException
    {
        return Class.forName(getMinecraftPackageName() + "." + name);
    }


    /**
     * Returns the value of a field (regardless of its visibility) for the given instance.
     *
     * @param hClass   The instance's class.
     * @param instance The instance.
     * @param name     The field's name.
     *
     * @return The field's value for the given instance.
     * @throws NoSuchFieldException     if the field does not exists.
     * @throws IllegalArgumentException if {@code instance} is not an instance of {@code hClass}.
     * @throws IllegalAccessException   if the field cannot be accessed due to a Java language
     *                                  access control.
     */
    static public Object getFieldValue(Class hClass, Object instance, String name)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        return getField(hClass, name).get(instance);
    }

    /**
     * Returns the value of a field (regardless of its visibility) for the given instance.
     *
     * @param instance The instance.
     * @param name     The field's name.
     *
     * @return The field's value for the given instance.
     * @throws NoSuchFieldException     if the field does not exists.
     * @throws IllegalArgumentException if {@code instance} is not an instance of itself (should
     *                                  never happens).
     * @throws IllegalAccessException   if the field cannot be accessed due to a Java language
     *                                  access control.
     */
    static public Object getFieldValue(Object instance, String name)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        return getFieldValue(instance.getClass(), instance, name);
    }


    /**
     * Makes the {@link Field} with the given name in the given class accessible, and returns it.
     *
     * @param klass The field's parent class.
     * @param name  The field's name.
     *
     * @return The {@link Field}.
     * @throws NoSuchFieldException if the class does not contains any field with this name.
     */
    static public Field getField(Class klass, String name) throws NoSuchFieldException
    {
        Field field = klass.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    /**
     * Finds the first {@link Field} in the given class with the given type, makes it accessible,
     * and returns it.
     *
     * @param klass The field's parent class.
     * @param type  The field's class.
     *
     * @return The {@link Field}.
     * @throws NoSuchFieldException if the class does not contains any field with this name.
     */
    static public Field getField(Class klass, Class type) throws NoSuchFieldException
    {
        for (Field field : klass.getDeclaredFields())
        {
            if (field.getType().equals(type))
            {
                field.setAccessible(true);
                return field;
            }
        }

        throw new NoSuchFieldException("Class " + klass.getName() + " does not define any field of type " + type.getName());
    }


    /**
     * Update the field with the given name in the given instance using the given value.
     *
     * @param instance The instance to update.
     * @param name     The name of the field to be updated.
     * @param value    The new value of the field.
     *
     * @throws NoSuchFieldException     if no field with the given name was found.
     * @throws IllegalArgumentException if {@code instance} is not an instance of itself (should
     *                                  never happens).
     * @throws IllegalAccessException   if the field cannot be accessed due to a Java language
     *                                  access control.
     */
    static public void setFieldValue(Object instance, String name, Object value)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        setFieldValue(instance.getClass(), instance, name, value);
    }

    /**
     * Update the field with the given name in the given instance using the given value.
     *
     * @param hClass   The field's parent class.
     * @param instance The instance to update.
     * @param name     The name of the field to be updated.
     * @param value    The new value of the field.
     *
     * @throws NoSuchFieldException     if no field with the given name was found.
     * @throws IllegalArgumentException if {@code instance} is not an instance of {@code hClass}.
     * @throws IllegalAccessException   if the field cannot be accessed due to a Java language
     *                                  access control.
     */
    static public void setFieldValue(Class hClass, Object instance, String name, Object value)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        getField(hClass, name).set(instance, value);
    }


    /**
     * Calls the given static method of the given class, passing the given parameters to it.
     *
     * @param hClass     The method's parent class.
     * @param name       The method's name.
     * @param parameters The parameters to be passed to the method.
     *
     * @return the object the called method returned.
     * @throws NoSuchMethodException     if no method with this name is defined in the class.
     * @throws IllegalAccessException    if the method cannot be accessed due to a Java language
     *                                   access control.
     * @throws IllegalArgumentException  if the method is an instance method; if the number of
     *                                   actual and formal parameters differ; if an unwrapping
     *                                   conversion for primitive arguments fails; or if, after
     *                                   possible unwrapping, a parameter value cannot be converted
     *                                   to the corresponding formal parameter type by a method
     *                                   invocation conversion.
     * @throws InvocationTargetException if an exception is thrown by the called method.
     */
    static public Object call(Class hClass, String name, Object... parameters)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return call(hClass, null, name, parameters);
    }

    /**
     * Calls the given method on the given instance, passing the given parameters to it.
     *
     * @param instance   The object the method is invoked from.
     * @param name       The method's name.
     * @param parameters The parameters to be passed to the method.
     *
     * @return the object the called method returned.
     * @throws NoSuchMethodException     if no method with this name is defined in the class.
     * @throws IllegalAccessException    if the method cannot be accessed due to a Java language
     *                                   access control.
     * @throws IllegalArgumentException  if the method is an instance method and the specified
     *                                   object argument is not an instance of the class or
     *                                   interface declaring the underlying method (or of a subclass
     *                                   or implementor thereof); if the number of actual and formal
     *                                   parameters differ; if an unwrapping conversion for
     *                                   primitive arguments fails; or if, after possible
     *                                   unwrapping, a parameter value cannot be converted to the
     *                                   corresponding formal parameter type by a method invocation
     *                                   conversion.
     * @throws InvocationTargetException if an exception is thrown by the called method.
     */
    static public Object call(Object instance, String name, Object... parameters)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return call(instance.getClass(), instance, name, parameters);
    }

    /**
     * Calls the given method on the given instance, passing the given parameters to it.
     *
     * @param hClass     The method's parent class.
     * @param instance   The object the method is invoked from.
     * @param name       The method's name.
     * @param parameters The parameters to be passed to the method.
     *
     * @return the object the called method returned.
     * @throws NoSuchMethodException     if no method with this name is defined in the class.
     * @throws IllegalAccessException    if the method cannot be accessed due to a Java language
     *                                   access control.
     * @throws IllegalArgumentException  if the method is an instance method and the specified
     *                                   object argument is not an instance of the class or
     *                                   interface declaring the underlying method (or of a subclass
     *                                   or implementor thereof); if the number of actual and formal
     *                                   parameters differ; if an unwrapping conversion for
     *                                   primitive arguments fails; or if, after possible
     *                                   unwrapping, a parameter value cannot be converted to the
     *                                   corresponding formal parameter type by a method invocation
     *                                   conversion.
     * @throws InvocationTargetException if an exception is thrown by the called method.
     */
    static public Object call(Class hClass, Object instance, String name, Object... parameters)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method method = hClass.getMethod(name, getTypes(parameters));
        return method.invoke(instance, parameters);
    }

    /**
     * Creates and returns an instance of the given class, passing the given parameters to the
     * appropriate constructor.
     *
     * @param hClass     The class to be instantiated.
     * @param parameters The parameters to be passed to the constructor. This also determines which
     *                   constructor will be called.
     *
     * @return the created instance.
     * @throws NoSuchMethodException     if no constructor with these parameters types exists.
     * @throws InstantiationException    if the class cannot be instantiated, due to a
     *                                   non-accessible or non-existent constructor, the class being
     *                                   an abstract one or an interface, a primitive type, or
     *                                   {@code void}.
     * @throws IllegalAccessException    if the constructor cannot be accessed due to a Java
     *                                   language access control.
     * @throws IllegalArgumentException  if the number of actual and formal parameters differ; if an
     *                                   unwrapping conversion for primitive arguments fails; or if,
     *                                   after possible unwrapping, a parameter value cannot be
     *                                   converted to the corresponding formal parameter type by a
     *                                   method invocation conversion; if this constructor pertains
     *                                   to an enum type.
     * @throws InvocationTargetException if an exception is thrown in the constructor.
     */
    static public Object instantiate(Class hClass, Object... parameters)
            throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Constructor constructor = hClass.getConstructor(getTypes(parameters));
        return constructor.newInstance(parameters);
    }


    /**
     * Returns the player's connection, frequently used to send packets.
     *
     * @param player The player.
     *
     * @return The player's connection (reflection-retrieved object).
     * @throws InvocationTargetException if an exception is thrown while the connection is
     *                                   retrieved.
     */
    static public Object getPlayerConnection(Player player) throws InvocationTargetException
    {
        try
        {
            Object craftPlayer = ReflectionUtils.getBukkitClassByName("entity.CraftPlayer").cast(player);
            Object handle = ReflectionUtils.call(craftPlayer, "getHandle");
            return ReflectionUtils.getFieldValue(handle, "playerConnection");
        }
        catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException e)
        {
            PluginLogger.error("Cannot retrieve standard Bukkit or NBS object, is the current Bukkit/Minecraft version supported by this API?", e);
            return null;
        }
    }

    /**
     * Sends a packet.
     *
     * @param playerConnection A player connection, as returned by the {@link #getPlayerConnection(Player)}
     *                         method.
     * @param packet           The packet to be sent, an instance of a subclass of the
     *                         net.minecraft.server.Packet class.
     *
     * @return {@code true} if the packet was successfully sent.
     * @throws InvocationTargetException if an exception is thrown while the packet is sent.
     */
    static public boolean sendPacket(Object playerConnection, Object packet) throws InvocationTargetException
    {
        try
        {
            final Class<?> packetClass = ReflectionUtils.getMinecraftClassByName("Packet");

            if(!packetClass.isAssignableFrom(packet.getClass()))
                return false;

            playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass).invoke(playerConnection, packet);
            return true;
        }
        catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException e)
        {
            return false;
        }
    }

    /**
     * Sends a packet.
     *
     * If you use this method, the player connection is not cached. If you have multiple packets to
     * send, store the player's connection returned by {@link #getPlayerConnection(Player)} and then
     * use the {@link #sendPacket(Object, Object)} method.
     *
     * @param player The player this packet will be sent to.
     * @param packet The packet to be sent, an instance of a subclass of the
     *               net.minecraft.server.Packet class.
     *
     * @return {@code true} if the packet was successfully sent.
     * @throws InvocationTargetException if an exception is thrown while the packet is sent.
     */
    static public boolean sendPacket(Player player, Object packet) throws InvocationTargetException
    {
        return sendPacket(getPlayerConnection(player), packet);
    }


    /**
     * Returns an array of the same size of the given array, containing the types of the objects in
     * the given array, in the same order.
     *
     * @param objects The original array.
     *
     * @return an array with the types of the items in the original array.
     */
    static public Class[] getTypes(Object[] objects)
    {
        Class[] types = new Class[objects.length];
        for (int i = 0; i < objects.length; i++)
        {
            types[i] = objects[i].getClass();
        }
        return types;
    }
}