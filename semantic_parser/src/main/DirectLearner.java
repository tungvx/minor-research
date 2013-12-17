/*
 * Author: Tamhd
 * Date: 09.09.2013
 */

package main;

import java.util.Arrays;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SGD;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class DirectLearner {
	// private Object which does the learning

	public double[] learn(List<Object[]> trainingData, int numberOfFeatures)
			throws Exception {
		// trainingData is the list of the form:
		// [[sample1], class1], [[sample2], class2].
		// numberOfFeatures is the size of the array [sample1]

		// Create the SGD
		Classifier cModel = (Classifier) new SGD();
		String[] option = weka.core.Utils
				.splitOptions("-F 0 -L 0.01 -R 1.0E-4 -E 500 -C 0.01");
		((SGD) cModel).setOptions(option);

		// Final class
		FastVector fvClassVal = new FastVector(2);
		fvClassVal.addElement("positive");
		fvClassVal.addElement("negative");
		Attribute ClassAttribute = new Attribute("theClass", fvClassVal);

		// Declare the feature vector
		FastVector fvWekaAttributes = new FastVector(numberOfFeatures + 1);
		for (int i = 0; i < numberOfFeatures; i++) {
			fvWekaAttributes.addElement(new Attribute("Numberic" + i));
		}

		fvWekaAttributes.addElement(ClassAttribute);

		// Create an empty training set
		Instances isTrainingSet = new Instances("Rel", fvWekaAttributes, 10);

		// Set class index
		isTrainingSet.setClassIndex(numberOfFeatures);

		// Create the instance
		for (int i = 0; i < trainingData.size(); i++) {
			Instance iExample = new DenseInstance(4);
			Object[] iObject = trainingData.get(i);
			double[] iInt = (double[]) iObject[0];
			for (int j = 0; j < numberOfFeatures; j++) {
				iExample.setValue((Attribute) fvWekaAttributes.elementAt(j),
						iInt[j]);
			}

			String iClass = (String) iObject[1];
			if (iClass.equalsIgnoreCase("positive")) {
				iExample.setValue((Attribute) fvWekaAttributes
						.elementAt(numberOfFeatures), "positive");
			} else {
				iExample.setValue((Attribute) fvWekaAttributes
						.elementAt(numberOfFeatures), "negative");
			}
			isTrainingSet.add(iExample);
		}
		cModel.buildClassifier(isTrainingSet);

		return ((SGD) cModel).getWeights(); // need to implement
	}
}
