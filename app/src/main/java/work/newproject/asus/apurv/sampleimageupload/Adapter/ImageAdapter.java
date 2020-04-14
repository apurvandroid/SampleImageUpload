package work.newproject.asus.apurv.sampleimageupload.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import work.newproject.asus.apurv.sampleimageupload.ModelUpload;
import work.newproject.asus.apurv.sampleimageupload.R;
import work.newproject.asus.apurv.sampleimageupload.interfaces.OnItemClickListener;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<ModelUpload> mUploads;
    public OnItemClickListener onItemClickListener;


    public ImageAdapter(Context context, List<ModelUpload> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public ImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ImageViewHolder holder, int position) {
        final ModelUpload uploadCurrent = mUploads.get(position);

        Picasso.get().load(uploadCurrent.getImage())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imageView);


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener = (OnItemClickListener) mContext;
                onItemClickListener.onClick(uploadCurrent.getImage());
            }
        });
    }
    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgShow);
        }
    }
}
