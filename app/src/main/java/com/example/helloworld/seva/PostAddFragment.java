package com.example.helloworld.seva;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebHistoryItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.nio.BufferUnderflowException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostAddFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostAddFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostAddFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseFood;
    private DatabaseReference mDatabaseClothes;
    private DatabaseReference mDatabaseBooks;
    private DatabaseReference mDatabaseMisc;

    private EditText mAddTitle;
    private EditText mAddDescription;
    private EditText mAddAddress;
    private EditText mAddDate;
    RadioGroup radioGroup;
    private TextView myImageViewText;
    private Button mAddPostBtn;
    ImageView datebtn;
    TextView expiryDate;
    Calendar myCalendar;
    ImageView imageView;

    int btn;

    View mView;

    private Button mPostBtn;

    private Uri mImageUri = null;

    private ProgressDialog mProgress;

    private String uId;
    private String name;
    private String contact;

    private static final int GALLERY_REQUEST = 1;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public PostAddFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PostAddFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostAddFragment newInstance(String param1, String param2) {
        PostAddFragment fragment = new PostAddFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_add, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseFood = FirebaseDatabase.getInstance().getReference().child("Food");
        mDatabaseClothes = FirebaseDatabase.getInstance().getReference().child("Clothes");
        mDatabaseBooks = FirebaseDatabase.getInstance().getReference().child("Books");
        mDatabaseMisc = FirebaseDatabase.getInstance().getReference().child("Misc");

        mStorage = FirebaseStorage.getInstance().getReference();

        radioGroup = (RadioGroup) mView.findViewById(R.id.radiogroup);

        mProgress = new ProgressDialog(getContext());

        mAddTitle = (EditText)mView.findViewById(R.id.addTitle);
        mAddDescription = (EditText)mView.findViewById(R.id.addDescription);
        mAddAddress = (EditText)mView.findViewById(R.id.addAddress);
        mAddDate = (EditText)mView.findViewById(R.id.addDate);
        myImageViewText = (TextView)mView.findViewById(R.id.myImageViewText);

        mAddPostBtn = (Button) mView.findViewById(R.id.addPostBtn);

        imageView = (ImageView) mView.findViewById(R.id.addimage);
        datebtn = (ImageView) mView.findViewById(R.id.pickexpdate);
        expiryDate = (TextView) mView.findViewById(R.id.addDate);

        btn = radioGroup.getCheckedRadioButtonId();
        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        mAddPostBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Boolean flag=true;
                if(TextUtils.isEmpty(mAddTitle.getText().toString().trim())){
                    mAddTitle.setError("Title can't be empty");
                    flag=false;
                }
                if(TextUtils.isEmpty(mAddDescription.getText().toString().trim())){
                    mAddDescription.setError("Description can't be empty");
                    flag=false;
                }
                if(TextUtils.isEmpty(mAddAddress.getText().toString().trim())){
                    mAddAddress.setError("Address can't be empty");
                    flag=false;
                }
                if(TextUtils.isEmpty(mAddDate.getText().toString().trim())){
                    mAddDate.setError("Date can't be empty");
                    flag=false;
                }
                if(flag)
                    addPost();
            }
        });

        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };


        datebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity()!=null) {
                    DatePickerDialog dpd = new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                    dpd.getDatePicker().setMinDate(System.currentTimeMillis());
                    dpd.show();
                }
                else
                    Toast.makeText(getContext(),"get context returned null in postAddFragment",Toast.LENGTH_SHORT).show();
            }
        });

        expiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity()!=null) {
                    DatePickerDialog dpd = new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                    dpd.getDatePicker().setMinDate(System.currentTimeMillis());
                    dpd.show();
                }
                else
                    Toast.makeText(getContext(),"get context returned null in postAddFragment",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateLabel() {
        String myFormat = "dd-MM-yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        expiryDate.setText(sdf.format(myCalendar.getTime()));
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String st) {
        if (mListener != null) {
            mListener.onFragmentInteraction(st);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityResult(int RequestCode, int resultCode, Intent data){
        super.onActivityResult(RequestCode,resultCode,data);

        if(RequestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .start(getContext(), this);
        }

        if (RequestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                imageView.setImageURI(mImageUri);
                myImageViewText.setVisibility(View.GONE);
                imageView.setAlpha((float)1.0);
                imageView.setBackgroundColor(Color.WHITE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void addPost(){

        mProgress.setMessage("Adding Post...");
        mProgress.show();

        uId = mAuth.getCurrentUser().getUid();

        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Map<String,String> > currMap = (Map<String,Map<String,String> >) dataSnapshot.getValue();

                final Map<String,String> userMap;
                userMap = currMap.get(uId);

                name = userMap.get("name");
                contact = userMap.get("contact");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final String title = mAddTitle.getText().toString().trim();
        final String description = mAddDescription.getText().toString().trim();
        final String address = mAddAddress.getText().toString().trim();
        final String date = expiryDate.getText().toString().trim();

        btn = radioGroup.getCheckedRadioButtonId();

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && !TextUtils.isEmpty(address) && !TextUtils.isEmpty(date) && mImageUri!=null){

            StorageReference filepath = mStorage.child("Post_Images").child(mImageUri.getLastPathSegment());

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUri = taskSnapshot.getDownloadUrl();

                    Date c = Calendar.getInstance().getTime();
                    System.out.println("Current time => " + c);

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = df.format(c);

                    if(btn==R.id.foodOption){
                        DatabaseReference newPost = mDatabaseFood.push();
                        final String newKey = newPost.getKey();
                        newPost.child("title").setValue(title);
                        newPost.child("description").setValue(description);
                        newPost.child("address").setValue(address);
                        newPost.child("date").setValue(date);
                        newPost.child("uid").setValue(uId);
                        newPost.child("name").setValue(name);
                        newPost.child("contact").setValue(contact);
                        newPost.child("image").setValue(downloadUri.toString());
                        newPost.child("imageUri").setValue(mImageUri.toString());
                        newPost.child("postdate").setValue(formattedDate);
                        newPost.child("iscompleted").setValue("false");

                        DatabaseReference userRef = mDatabaseUsers.child(uId);
                        final DatabaseReference foodRef = userRef.child("myads").child("Food");

                        foodRef.child(newKey).setValue("true");
                    }

                    if(btn==R.id.clothesOption){
                        DatabaseReference newPost = mDatabaseClothes.push();
                        final String newKey = newPost.getKey();
                        newPost.child("title").setValue(title);
                        newPost.child("description").setValue(description);
                        newPost.child("address").setValue(address);
                        newPost.child("date").setValue(date);
                        newPost.child("uid").setValue(uId);
                        newPost.child("name").setValue(name);
                        newPost.child("contact").setValue(contact);
                        newPost.child("image").setValue(downloadUri.toString());
                        newPost.child("imageUri").setValue(mImageUri.toString());
                        newPost.child("postdate").setValue(formattedDate);
                        newPost.child("iscompleted").setValue("false");

                        DatabaseReference userRef = mDatabaseUsers.child(uId);
                        final DatabaseReference clothesRef = userRef.child("myads").child("Clothes");

                        clothesRef.child(newKey).setValue("true");
                    }

                    if(btn==R.id.booksOption){
                        DatabaseReference newPost = mDatabaseBooks.push();
                        final String newKey = newPost.getKey();
                        newPost.child("title").setValue(title);
                        newPost.child("description").setValue(description);
                        newPost.child("address").setValue(address);
                        newPost.child("date").setValue(date);
                        newPost.child("uid").setValue(uId);
                        newPost.child("name").setValue(name);
                        newPost.child("contact").setValue(contact);
                        newPost.child("image").setValue(downloadUri.toString());
                        newPost.child("imageUri").setValue(mImageUri.toString());
                        newPost.child("postdate").setValue(formattedDate);
                        newPost.child("iscompleted").setValue("false");

                        DatabaseReference userRef = mDatabaseUsers.child(uId);
                        final DatabaseReference booksRef = userRef.child("myads").child("Books");

                        booksRef.child(newKey).setValue("true");

                    }

                    if(btn==R.id.miscOption){
                        DatabaseReference newPost = mDatabaseMisc.push();
                        final String newKey = newPost.getKey();
                        newPost.child("title").setValue(title);
                        newPost.child("description").setValue(description);
                        newPost.child("address").setValue(address);
                        newPost.child("date").setValue(date);
                        newPost.child("uid").setValue(uId);
                        newPost.child("name").setValue(name);
                        newPost.child("contact").setValue(contact);
                        newPost.child("image").setValue(downloadUri.toString());
                        newPost.child("imageUri").setValue(mImageUri.toString());
                        newPost.child("postdate").setValue(formattedDate);
                        newPost.child("iscompleted").setValue("false");

                        DatabaseReference userRef = mDatabaseUsers.child(uId);
                        final DatabaseReference miscRef = userRef.child("myads").child("Misc");

                        miscRef.child(newKey).setValue("true");
                    }

                    //newPost.child("uid").setValue(FirebaseAuth.getCurrUser().getUid());

                    mProgress.dismiss();

                    Log.e("cn",getFragmentManager().getBackStackEntryCount()+"");
                    Intent mainIntent = new Intent(getContext(), MainActivity.class);
                    //mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            });

        }
        else{
            mProgress.dismiss();
            if(mImageUri==null){
                Toast.makeText(getContext(),"Please add image",Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String st);
    }
}
