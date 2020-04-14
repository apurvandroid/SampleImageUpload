package work.newproject.asus.apurv.sampleimageupload.FCM;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

import work.newproject.asus.apurv.sampleimageupload.MainActivity;
import work.newproject.asus.apurv.sampleimageupload.ModelUpload;
import work.newproject.asus.apurv.sampleimageupload.interfaces.OnItemClickListener;
import work.newproject.asus.apurv.sampleimageupload.interfaces.getStatus;


public class UploadImage {
    public static void uploadImg(final Context context, Uri uri) {
        StorageTask mUploadTask;
        final getStatus getStatus;

        FirebaseStorage storage;
        StorageReference storageReference;
        final DatabaseReference mDatabaseRef;
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        final StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());

        getStatus = (getStatus) context;
        mUploadTask = ref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String value = String.valueOf(uri);
                                ModelUpload upload = new ModelUpload();
                                upload.setImage(value);
                                String uploadId = mDatabaseRef.push().getKey();
                                mDatabaseRef.child(uploadId).setValue(upload);
                                Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show();
                            }

                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                 //       loading.setMessage("Uploaded " + (int) progress + "%");
                        getStatus.onClick(progress);
                    }
                });

    }


}
