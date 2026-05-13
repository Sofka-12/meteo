package com.example.meteopipli.domain.ml

import kotlin.math.exp

class LogisticRegression(
    private val learningRate: Double = 0.01,
    private val iterations: Int = 500
) {
    private var weights: DoubleArray = doubleArrayOf()
    private var bias: Double = 0.0

    // features: список из DoubleArray (температура, давление, влажность, kpIndex)
    // labels: 0 – хорошо (feelingScore > 2), 1 – плохо (feelingScore <= 2)
    fun train(features: List<DoubleArray>, labels: List<Int>) {
        if (features.isEmpty()) return
        val nFeatures = features[0].size
        weights = DoubleArray(nFeatures) { 0.0 }
        bias = 0.0

        repeat(iterations) {
            var dw = DoubleArray(nFeatures) { 0.0 }
            var db = 0.0

            for (i in features.indices) {
                val z = features[i].mapIndexed { j, x -> x * weights[j] }.sum() + bias
                val prediction = sigmoid(z)
                val error = prediction - labels[i]

                for (j in 0 until nFeatures) {
                    dw[j] += error * features[i][j]
                }
                db += error
            }

            for (j in 0 until nFeatures) {
                weights[j] -= learningRate * dw[j] / features.size
            }
            bias -= learningRate * db / features.size
        }
    }

    fun predict(features: DoubleArray): Double {
        require(weights.isNotEmpty()) { "Model not trained yet" }
        val z = features.mapIndexed { i, x -> x * weights[i] }.sum() + bias
        return sigmoid(z)
    }

    private fun sigmoid(z: Double): Double = 1.0 / (1.0 + exp(-z))

    fun getWeights(): DoubleArray = weights
    fun getBias(): Double = bias
    fun setWeightsAndBias(weights: DoubleArray, bias: Double) {
        this.weights = weights
        this.bias = bias
    }
}