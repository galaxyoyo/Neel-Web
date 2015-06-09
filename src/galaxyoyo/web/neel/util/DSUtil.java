package galaxyoyo.web.neel.util;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.ObjectifyService;

public class DSUtil
{
	private static final List<Class<?>> classes = new ArrayList<Class<?>>();
	
	public static <T> T load(Class<T> classOfT, String id) throws NotFoundException
	{
		register(classOfT);
		return load(classOfT, id, true);
	}
	

	public static <T> T load(Class<T> classOfT, long id) throws NotFoundException
	{
		register(classOfT);
		return load(classOfT, id, true);
	}
	
	public static <T> T load(Class<T> classOfT, String id, boolean throwEx) throws NotFoundException
	{
		register(classOfT);
		LoadResult<T> result = ofy().load().key(Key.create(classOfT, id));
		if (throwEx)
			return result.safe();
		else
			return result.now();
	}
	
	public static <T> List<T> list(Class<T> classOfT)
	{
		register(classOfT);
		return ofy().load().type(classOfT).list();
	}
	
	public static <T> T load(Class<T> classOfT, long id, boolean throwEx) throws NotFoundException
	{
		register(classOfT);
		LoadResult<T> result = ofy().load().key(Key.create(classOfT, id));
		if (throwEx)
			return result.safe();
		else
			return result.now();
	}
	
	public static <T> Key<T> save(T obj)
	{
		register(obj.getClass());
		return ofy().save().entity(obj).now();
	}
	
	@SafeVarargs
	public static <T> Map<Key<T>, T> save(T ... objs)
	{
		return ofy().save().entities(objs).now();
	}
	
	public static <T> Void delete(T obj)
	{
		register(obj.getClass());
		return ofy().delete().entity(obj).now();
	}
	
	@SafeVarargs
	public static <T> Void delete(T ... objs)
	{
		return ofy().delete().entities(objs).now();
	}
	
	public static <T> Void delete(Class<T> classOfT, String id)
	{
		register(classOfT);
		return ofy().delete().key(Key.create(classOfT, id)).now();
	}
	
	public static <T> Void delete(Class<T> classOfT, long id)
	{
		register(classOfT);
		return ofy().delete().key(Key.create(classOfT, id)).now();
	}
	
	public static <T> void register(Class<T> classOfT)
	{
		if (!classes.contains(classOfT))
		{
			ObjectifyService.register(classOfT);
			classes.add(classOfT);
		}
	}
}
