package work.newproject.asus.apurv.sampleimageupload;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.zolad.zoominimageview.ZoomInImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullScreenZoomActivity extends AppCompatActivity {
    @BindView(R.id.zoomImg)
    ZoomInImageView zoomImg;
    String imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_zoom);
        ButterKnife.bind(this);
        imageUri = getIntent().getStringExtra("path");
        Picasso.get().load(imageUri).into(zoomImg);


    }
}
