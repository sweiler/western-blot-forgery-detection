package flann;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;


public interface Flann extends Library {
	Flann INSTANCE = (Flann) Native.loadLibrary("flann", Flann.class);

	public static class FLANNParameters extends Structure {

		public int algorithm; /* the algorithm to use */

		/* search time parameters */
		public int checks; /* how many leafs (features) to check in one search */
		public float eps; /* eps parameter for eps-knn search */
		public int sorted; /*
							 * indicates if results returned by radius search
							 * should be sorted or not
							 */
		public int max_neighbors; /*
								 * limits the maximum number of neighbors should
								 * be returned by radius search
								 */
		public int cores; /* number of paralel cores to use for searching */

		/* kdtree index parameters */
		public int trees; /* number of randomized trees to use (for kdtree) */
		public int leaf_max_size;

		/* kmeans index parameters */
		public int branching; /* branching factor (for kmeans tree) */
		public int iterations; /*
								 * max iterations to perform in one kmeans
								 * cluetering (kmeans tree)
								 */
		public int centers_init; /*
								 * algorithm used for picking the initial
								 * cluster centers for kmeans tree
								 */
		public float cb_index; /*
								 * cluster boundary index. Used when searching
								 * the kmeans tree
								 */

		/* autotuned index parameters */
		public float target_precision; /*
										 * precision desired (used for
										 * autotuning, -1 otherwise)
										 */
		public float build_weight; /* build tree time weighting factor */
		public float memory_weight; /* index memory weigthing factor */
		public float sample_fraction; /*
									 * what fraction of the dataset to use for
									 * autotuning
									 */

		/* LSH parameters */
		public int table_number_;
		/** The number of hash tables to use */
		public int key_size_;
		/** The length of the key in the hash tables */
		public int multi_probe_level_;
		/** Number of levels to use in multi-probe LSH, 0 for standard LSH */

		/* other parameters */
		public int log_level; /* determines the verbosity of each flann function */
		public long random_seed; /* random seed to use */

		@Override
		protected List<String> getFieldOrder() {

			return Arrays.asList("algorithm", "checks", "eps", "sorted",
					"max_neighbors", "cores", "trees", "leaf_max_size",
					"branching", "iterations", "centers_init", "cb_index",
					"target_precision", "build_weight", "memory_weight",
					"sample_fraction", "table_number_", "key_size_",
					"multi_probe_level_", "log_level", "random_seed");
		}

	}

	/**
	 * Sets the distance type to use throughout FLANN. If distance type
	 * specified is MINKOWSKI, the second argument specifies which order the
	 * minkowski distance should have.
	 */
	public void flann_set_distance_type(int distance_type, int order);

	/**
	 * Builds and returns an index. It uses autotuning if the target_precision
	 * field of index_params is between 0 and 1, or the parameters specified if
	 * it's -1.
	 * 
	 * @param dataset
	 *            pointer to a data set stored in row major order
	 * @param rows
	 *            number of rows (features) in the dataset
	 * @param cols
	 *            number of columns in the dataset (feature dimensionality)
	 * @param speedup
	 *            speedup over linear search, estimated if using autotuning,
	 *            output parameter
	 * @param index_params
	 *            index related parameters
	 * @param flann_params
	 *            generic flann parameters
	 * 
	 * @return the newly created index or a number <0 for error
	 */
	public Pointer flann_build_index_double(double[] dataset, int rows,
			int cols, FloatByReference speedup, FLANNParameters flann_params);

	/**
	 * Searches for nearest neighbors using the index provided
	 * 
	 * @param index_id
	 *            the index (constructed previously using flann_build_index).
	 * @param testset
	 *            pointer to a query set stored in row major order
	 * @param trows
	 *            number of rows (features) in the query dataset (same
	 *            dimensionality as features in the dataset)
	 * @param indices
	 *            pointer to matrix for the indices of the nearest neighbors of
	 *            the testset features in the dataset (must have trows number of
	 *            rows and nn number of columns)
	 * @param dists
	 *            pointer to matrix for the distances of the nearest neighbors
	 *            of the testset features in the dataset (must have trows number
	 *            of rows and 1 column)
	 * @param nn
	 *            how many nearest neighbors to return
	 * @param flann_params
	 *            generic flann parameters
	 * 
	 * @return zero or a number <0 for error
	 */
	public int flann_find_nearest_neighbors_index_double(Pointer index_id,
			double[] testset, int trows, int[] indices, double[] dists, int nn,
			FLANNParameters flann_params);

	/**
	 * Builds an index and uses it to find nearest neighbors.
	 * 
	 * @param dataset
	 *            pointer to a data set stored in row major order
	 * @param rows
	 *            number of rows (features) in the dataset
	 * @param cols
	 *            number of columns in the dataset (feature dimensionality)
	 * @param testset
	 *            pointer to a query set stored in row major order
	 * @param trows
	 *            number of rows (features) in the query dataset (same
	 *            dimensionality as features in the dataset)
	 * @param indices
	 *            pointer to matrix for the indices of the nearest neighbors of
	 *            the testset features in the dataset (must have trows number of
	 *            rows and nn number of columns)
	 * @param nn
	 *            how many nearest neighbors to return
	 * @param flann_params
	 *            generic flann parameters
	 * 
	 * @return zero or -1 for error
	 */
	public int flann_find_nearest_neighbors_double(double[] dataset, int rows,
			int cols, double[] testset, int trows, int[] indices,
			double[] dists, int nn, FLANNParameters flann_params);

	/**
	 * Deletes an index and releases the memory used by it.
	 * 
	 * @param index_id
	 *            the index (constructed previously using flann_build_index).
	 * @param flann_params
	 *            generic flann parameters
	 * 
	 * @return zero or a number <0 for error
	 */
	public int flann_free_index_double(Pointer index_id,
			FLANNParameters flann_params);
}
