package com.um.climateready.feature.resources

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.um.climateready.core.model.ClimateResource
import com.um.climateready.databinding.ItemResourceCardBinding

class ResourceAdapter(
    private var resources: List<ClimateResource>
) : RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder>() {

    inner class ResourceViewHolder(
        val binding: ItemResourceCardBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val binding = ItemResourceCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ResourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        val resource = resources[position]
        val context = holder.itemView.context
        with(holder.binding) {
            tvResourceName.text = resource.name
            tvResourceBadge.text = "${categoryLabel(resource.category)} • Verified"
            tvResourceDescription.text = resource.shortDescription
            tvResourceOrg.text = resource.sourceOrganization
            tvResourcePhone.text = "Hotline: ${resource.phoneNumber}"

            btnCall.setOnClickListener {
                if (resource.phoneNumber == "N/A") {
                    Toast.makeText(context, "No hotline available for this resource", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${resource.phoneNumber}"))
                    context.startActivity(intent)
                }
            }

            btnVisit.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.officialUrl))
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "No browser available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = resources.size

    fun updateData(newResources: List<ClimateResource>) {
        resources = newResources
        notifyDataSetChanged()
    }

    private fun categoryLabel(category: String): String = when (category) {
        "HEAVY_RAIN" -> "Heavy Rain"
        "EXTREME_HEAT" -> "Extreme Heat"
        "HAZE_AIR_QUALITY" -> "Haze & AQI"
        "CLIMATE_CHANGE" -> "Climate Change"
        else -> category
    }
}
