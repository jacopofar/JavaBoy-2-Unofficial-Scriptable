package machinelearning;


/**
 * A naïve implementation of a naïve Bayes classifier.
 * Thought it should work, it's intended as a stub to be replaced by a library
 * */
public class NaiveBayes {


	private String[] classes;
	private int[][] counts;

	/**
	 * Create a Naïve Bayes Classifier which will be trained and will recognize boolean vectors of the given size assigned to a class from the given classes.
	 * Features inside the vector are identified by an integer index, classes through a String.
	 * The classifier uses Laplace smoothing (that is, it adds 1 to each feature-case pair to contrast overfitting)
	 * */
	public NaiveBayes(int featureNumber, String[] classes){
		this.classes=classes.clone();
		this.counts=new int[featureNumber][classes.length];
	}

	/**
	 * Add a feature vector and class pair to the model
	 * */
	public void train(boolean[] input,String outputClass){
		//verify that the class is present, and what's the index value
		int index=-1;
		for(int i=0;i<classes.length;i++)
			if(classes[i].equals(outputClass)) index=i;
		if(index==-1) throw new UnknownClassException(outputClass);
		//iterate over all features
		for(int i=0;i<counts.length;i++)
			if(input[i])counts[i][index]++;
	}

	//return the most likely class for the input vector
	public String classify(boolean[] input){
		double[] scores=new double[classes.length];
		//calculates likelihood, use logarithm properties to calculate it accurately
		//also uses Laplace smoothing
		for(int i=0;i<counts.length;i++)
			for(int s=0;s<scores.length;s++)
				if(input[s])
					scores[s]+=Math.log(1.0+(double)counts[i][s]);
		//find maximum value
		double foundmax=Double.NEGATIVE_INFINITY;
		String result=null;
		for(int s=0;s<scores.length;s++)
			if(scores[s]>foundmax){
				result=classes[s];
				foundmax=scores[s];
			}
		return result;
	}

	public static void main(String argc[]){	
		NaiveBayes b = new NaiveBayes(4,new String[]{"rainy","sunny","cloudy"});
		b.train(new boolean[]{false, false,false,true}, "sunny");
		b.train(new boolean[]{false, false,false,true}, "sunny");
		b.train(new boolean[]{false, true,false,false}, "cloudy");
		b.train(new boolean[]{false, true,false,false}, "cloudy");
		b.train(new boolean[]{true, false,false,false}, "rainy");
		b.train(new boolean[]{true,false,true,true}, "rainy");
		System.out.println("f,f,t,t is classified as "+b.classify(new boolean[]{false,false,true,true}));
	}

	/**
	 * Exception thrown when a class sent for training is not present inside the class vector
	 * */
	public class UnknownClassException extends RuntimeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String wrongClass;
		public UnknownClassException(String outputClass) {
			this.wrongClass=outputClass;
		}
		public String getMessage(){
			return "the class "+wrongClass+" was received for training but is not present in classes vector";

		}

	}

}
