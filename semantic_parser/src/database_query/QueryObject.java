package database_query;

import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jpl.Atom;
import jpl.Compound;
import jpl.Query;
import jpl.Term;
import jpl.Variable;
import utils.DataConfig;

public class QueryObject {
	private static QueryObject queryObject;

	public static QueryObject getQueryObject() {
		if (queryObject == null)
			queryObject = new QueryObject();
		return queryObject;
	}

	private QueryObject() {
		Query q1 = new Query("consult", new Term[] { new Atom(
				"./database/geoquery") });
		q1.query();
	}

	public void test() {
		Variable X = new Variable("X");
		Variable S = new Variable("S");
		Compound compound = new Compound("stateid", new Term[] { X });
		Compound compound2 = new Compound("size", new Term[] { compound, S });
		Query query = new Query("largest", new Term[] { S, compound2 });
		while (query.hasMoreElements()) {
			Hashtable binding = (Hashtable) query.nextElement();
			Term t = (Term) binding.get("X");
			System.out.println(t);
		}
	}

	private static ExecutorService executor = Executors.newCachedThreadPool();

	public static Term execute(final String queryString) {

		Future<Term> future = executor.submit(new CustomCallable(queryString));
		try {
			return future.get(DataConfig.TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean checkExist(String queryString) {
		Query query = new Query(queryString);
		return query.hasMoreElements();
	}
}
