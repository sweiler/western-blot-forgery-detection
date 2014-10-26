package forgery.util;

public class ZernikeCPU implements Runnable {
	
	static int[] factorials = new int[] { 1, 1, 2, 6, 24, 120 };
	static int[] host_factorials = new int[] { 1, 1, 2, 6, 24, 120 };

	private static final int FEATURE_SIZE = 14;
	private static final int MAX_ZERNIKE_DEGREE = 5;
	
	
	private int block_size, width, height, range_threshold,
			avg_intensity_threshold, xIndex, yIndex;
	private byte[] input_image;
	private double[] result_matrix;

	public ZernikeCPU(int block_size, int width, int height,
			byte[] input_image, int range_threshold,
			int avg_intensity_threshold, double[] result_matrix, int x, int y) {
		this.block_size = block_size;
		this.width = width;
		this.height = height;
		this.range_threshold = range_threshold;
		this.avg_intensity_threshold = avg_intensity_threshold;
		this.input_image = input_image;
		this.result_matrix = result_matrix;
		this.xIndex = x;
		this.yIndex = y;
	}

	@Override
	public void run() {
		if (xIndex > (width - block_size) || yIndex > (height - block_size)) {
			return;
		}

		// get left-upper index of the block to calculate
		int block_start_pos = xIndex * height + yIndex;
		int result_row_start_pos = (xIndex * (height - block_size + 1) + yIndex)
				* FEATURE_SIZE;

		if (range_threshold > 0
				&& getRange(block_size, input_image, block_start_pos, height) <= range_threshold) {
			result_matrix[result_row_start_pos + 12] = -1.0;
			result_matrix[result_row_start_pos + 13] = -1.0;
			return;
		}

		if (avg_intensity_threshold > 0
				&& getAverageIntensity(block_size, input_image,
						block_start_pos, height) >= avg_intensity_threshold) {
			result_matrix[result_row_start_pos + 12] = -1.0;
			result_matrix[result_row_start_pos + 13] = -1.0;
			return;
		}

		int n;
		int m;
		int i = 0;
		int x;
		int y;
		double moment_real;
		double moment_img;
		double[] feature_vector = new double[FEATURE_SIZE];
		double radius = ((double) (block_size - 1)) / 2;

		for (n = 0; n <= MAX_ZERNIKE_DEGREE; n++) {
			for (m = 0; m <= n; m++) {
				if (((n - m) % 2) == 1) {
					continue;
				}
				moment_real = 0.0;
				moment_img = 0.0;
				for (y = 0; y < block_size; y++) {
					for (x = 0; x < block_size; x++) {
						// convert to polar coordinates
						double rho = getRhoFromCartesian(x - radius, y - radius);
						double theta = getThetaFromCartesian(x - radius, y
								- radius);
						// scale rho to unit radius
						rho = rho / radius;
						if (rho > 1) {
							continue;
						}
						DoubleComplex zernikeMoment = getZernikeMoment(n, m,
								rho, theta);
						moment_real += zernikeMoment.real
								* (input_image[block_start_pos
										+ (x * height + y)] & 0xFF);
						moment_img += zernikeMoment.imaginary
								* (input_image[block_start_pos
										+ (x * height + y)] & 0xFF);
					}
				}
				// normalize
				moment_real *= ((n + 1) / Math.PI);
				moment_img *= ((n + 1) / Math.PI);
				// take the magnitude
				feature_vector[i] = Math.sqrt(moment_real * moment_real
						+ moment_img * moment_img);
				i++;
			}
		}
		for (i = 0; i < FEATURE_SIZE - 2; i++) {
			result_matrix[result_row_start_pos + i] = feature_vector[i];
		}
		result_matrix[result_row_start_pos + 12] = xIndex;
		result_matrix[result_row_start_pos + 13] = yIndex;
	}

	protected static DoubleComplex getZernikeMoment(int n, int m, double rho,
			double theta) {
		double zernikePolynomial = getZernikeRadialPolynomial(n, m, rho);
		// using Eulers Formula to express e^(i*m*theta) in real and imaginary
		return new DoubleComplex(zernikePolynomial * Math.cos(m * theta),
				(-1) * zernikePolynomial * Math.sin(m * theta));
	}

	protected static double getZernikeRadialPolynomial(int n, int m, double rho) {
		double result = 0.0;
		if ((n - m) % 2 != 0) { // Rn,m is zero for uneven (n-m)
			return result;
		}
		int s = 0;
		for (; s <= (int) ((n - m) / 2); s++) {
			double tmp = (Math.pow(-1,s) * host_factorials[n - s] *
					Math.pow(rho, (n - 2 * s))) /
			(host_factorials[s] *
					host_factorials[((n + m) / 2) - s] *
					host_factorials[((n - m) / 2) - s]);
			result += tmp;
		}
		return result;
	}

	protected static double getThetaFromCartesian(double x, double y) {
		if (x == 0 && y == 0) {
			return 0.0;
		}
		return Math.atan2(x, y);
	}

	protected static double getRhoFromCartesian(double x, double y) {
		return Math.sqrt((x * x + y * y));
	}

	protected static double getAverageIntensity(int block_size, byte[] input_image,
			int block_start_pos, int height) {
		int x;
		int y;
		double avg = 0.0;
		for (y = 0; y < block_size; y++) {
			for (x = 0; x < block_size; x++) {
				avg += input_image[block_start_pos + (x * height + y)] & 0xFF;
			}
		}
		return avg / ((double) (block_size * block_size));
	}

	protected static int getRange(int block_size, byte[] input_image,
			int block_start_pos, int height) {
		int min = 255;
		int max = 0;
		int x;
		int y;
		for (y = 0; y < block_size; y++) {
			for (x = 0; x < block_size; x++) {
				if ((input_image[block_start_pos + (x * height + y)] & 0xFF) > max) {
					max = input_image[block_start_pos + (x * height + y)] & 0xFF;
				}
				if ((input_image[block_start_pos + (x * height + y)] & 0xFF) < min) {
					min = input_image[block_start_pos + (x * height + y)] & 0xFF;
				}
			}
		}
		return max - min;
	}

}
