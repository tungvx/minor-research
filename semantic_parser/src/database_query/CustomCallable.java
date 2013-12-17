package database_query;

import java.util.Hashtable;
import java.util.concurrent.Callable;

import jpl.Atom;
import jpl.PrologException;
import jpl.Query;
import jpl.Term;

public class CustomCallable implements Callable<Term> {
	private String queryString;

	public CustomCallable(String queryString) {
		this.queryString = queryString;
	}

	@Override
	public Term call() throws Exception {
		Query query = new Query("execute_query(" + queryString + ", Result)");
		try {
			if (query.hasMoreElements()) {
				return (Term) ((Hashtable) query.nextElement()).get("Result");
			}
		} catch (PrologException e) {
			e.printStackTrace();
		}
		return null;
	}
}
