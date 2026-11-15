package com.hrm.diagram.render.family

import com.hrm.diagram.core.ir.Cluster
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.render.cache.DrawEntityKey

internal object GraphEntityKeys {
    fun keys(prefix: String, model: GraphIR): List<String> {
        val keys = ArrayList<String>()
        flattenClusters(model.clusters).forEach { keys += DrawEntityKey.cluster(prefix, it.id) }
        model.edges.forEachIndexed { index, edge -> keys += DrawEntityKey.edge(prefix, edge.from, edge.to, index) }
        model.nodes.forEach { keys += DrawEntityKey.node(prefix, it.id) }
        return keys
    }

    private fun flattenClusters(clusters: List<Cluster>): List<Cluster> =
        clusters.flatMap { cluster -> listOf(cluster) + flattenClusters(cluster.nestedClusters) }
}
