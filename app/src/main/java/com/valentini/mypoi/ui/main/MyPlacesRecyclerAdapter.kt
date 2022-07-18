import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.valentini.mypoi.R
import com.valentini.mypoi.ui.main.MyPlace

class MyPlacesRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var items : List<MyPlace> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyPlacesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder)
        {
            is MyPlacesViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(myplacesList: List<MyPlace>)
    {
        items = myplacesList
    }

    class MyPlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val myplace_name = "ciao"
        val place_latitude = 1.3
        val place_longitude = 1.3

        fun bind(myplace : MyPlace)
        {
            val requestOptions = RequestOptions().placeholder(R.drawable.casa).error(R.drawable.ic_add_maps)
            val load = Glide.with(itemView.context).applyDefaultRequestOptions(requestOptions)
                .load(myplace.myplace_name)
        }
    }
}
