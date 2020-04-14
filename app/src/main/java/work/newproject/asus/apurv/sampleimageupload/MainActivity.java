package work.newproject.asus.apurv.sampleimageupload;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;
import com.zolad.zoominimageview.ZoomInImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import work.newproject.asus.apurv.sampleimageupload.Adapter.ImageAdapter;
import work.newproject.asus.apurv.sampleimageupload.FCM.UploadImage;
import work.newproject.asus.apurv.sampleimageupload.interfaces.OnItemClickListener;
import work.newproject.asus.apurv.sampleimageupload.interfaces.getStatus;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnItemClickListener, getStatus {

    private ImageAdapter mAdapter;
    ProgressDialog loading;
    final double TOTAL_PROGRESS=100.0;
    @BindView(R.id.progressBar)
    ProgressBar mProgressCircle;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    private static int PERMISSION_ALL = 1;
    String[] PERMISSIONS;

    @BindView(R.id.btFlotting)
    FloatingActionButton btFlotting;
    private List<ModelUpload> mUploads;
    private DatabaseReference databaseReference;
    public Uri imgUri;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        showList();

        btFlotting.setOnClickListener(this);
        PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET};
        databaseReference = FirebaseDatabase.getInstance().getReference("data");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btFlotting:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!hasPermissions(this, PERMISSIONS)) {
                        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                    } else {
                        pickImg(this);
                    }
                } else {
                    pickImg(this);
                }
        }


    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_ALL) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImg(this);
            } else {
                Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }


    private void pickImg(Context context) {
        CropImage.startPickImageActivity(MainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = CropImage.getPickImageResultUri(this, data);
            stratCroping(uri);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                showDialog(activityResult.getUri());
            }

        }
    }

    private void stratCroping(Uri uri) {
        CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true).start(this);
    }

    private void showList() {
        showProgress();
        mUploads = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("uploads");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUploads = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    ModelUpload upload = postSnapshot.getValue(ModelUpload.class);
                    mUploads.add(upload);
                    hideProgress();
                }

                mAdapter = new ImageAdapter(MainActivity.this, mUploads);
                recyclerView.setAdapter(mAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgress();
            }
        });
    }

    private void showProgress() {
        mProgressCircle.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressCircle.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(String imageURi) {
        Intent intent = new Intent(MainActivity.this, FullScreenZoomActivity.class);
        intent.putExtra("path", imageURi);
        startActivity(intent);
    }


    private void showDialog(final Uri uri) {
        //      imagePicker.setImageURI(activityResult.getUri());
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.image_layout, null);

        final ImageView imagePicker = view.findViewById(R.id.imagePicker);
        final Button btUpload = view.findViewById(R.id.btUpload);
        Picasso.get().load(uri).into(imagePicker);

        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri != null) {
                    UploadImage.uploadImg(MainActivity.this, uri);
                }
            }
        });
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    @Override
    public void onClick(double status) {
        if (status == TOTAL_PROGRESS) {
            loading.dismiss();
            alertDialog.dismiss();
        } else {
            loading = ProgressDialog.show(this, "", "Please wait ", false, true);


        }
    }
}