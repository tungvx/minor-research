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
		// Query q1 = new Query("consult", new Term[] { new Atom(
		// "./database/geoquery") });
		// q1.query();
		Query query = new Query("execute_query(" + queryString + ", Result)");
		try {
			if (query.hasMoreElements()) {
<<<<<<< HEAD
				return (Term) ((Hashtable) query.nextElement()).get("Result");
=======
				Term result = (Term) ((Hashtable) query.nextElement())
						.get("Result");
				System.out.println(result);
				return result;
>>>>>>> c1cd19f... fix bugs
			}
		} catch (PrologException e) {
			e.printStackTrace();
		}
		return null;
	}

}
