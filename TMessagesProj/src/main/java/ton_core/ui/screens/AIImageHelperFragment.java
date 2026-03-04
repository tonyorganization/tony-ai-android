package ton_core.ui.screens;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.BasePermissionsActivity;
import org.telegram.ui.Components.ChatAttachAlert;
import org.telegram.ui.Components.ChatAttachAlertDocumentLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import ton_core.repositories.translated_message_repository.image_repository.IImageRepository;
import ton_core.repositories.translated_message_repository.image_repository.ImageRepository;
import ton_core.services.IOnApiCallback;
import ton_core.shared.CheckerboardDrawable;
import ton_core.shared.Constants;
import ton_core.ui.dialogs.LoadingDialog;

public class AIImageHelperFragment extends BaseFragment implements ChatAttachAlertDocumentLayout.DocumentSelectActivityDelegate {

    private final int type;
    private LinearLayout llPickImage;
    private LinearLayout llAction;
    private ImageView ivSelectedImage;
    private CardView cvAction;
    private TextView tvAction;

    private String currentPicturePath;

    private ChatAttachAlert chatAttachAlert;
    private final IImageRepository imageRepository;
    private final LoadingDialog loadingDialog;
    private File cacheFile;

    public AIImageHelperFragment(int type) {
        this.type = type;
        imageRepository = ImageRepository.getInstance();
        loadingDialog = new LoadingDialog();
    }

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void dismissCurrentDialog() {
        if (chatAttachAlert != null && visibleDialog == chatAttachAlert) {
            chatAttachAlert.getPhotoLayout().closeCamera(false);
            chatAttachAlert.dismissInternal();
            chatAttachAlert.getPhotoLayout().hideCamera(true);
            return;
        }
        super.dismissCurrentDialog();
    }

    private void requestPermissions(Context context) {
        final Activity activity = getParentActivity();

        if (Build.VERSION.SDK_INT >= 23) {
            try {
                activity.requestPermissions(new String[]{Manifest.permission.CAMERA}, 18);
            } catch (Exception ignore) {
            }
            if (Build.VERSION.SDK_INT >= 33) {
                try {
                    if (context.checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED || context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                        activity.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES}, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
                    }
                } catch (Exception ignore) {
                }
            } else {
                try {
                    if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
                    }
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void createChatAttachView() {
        if (getParentActivity() == null || getContext() == null) {
            return;
        }
        if (chatAttachAlert == null) {
            chatAttachAlert = new ChatAttachAlert(getParentActivity(), this, false, false, true, resourceProvider) {
                @Override
                public void dismissInternal() {
                    if (chatAttachAlert != null && chatAttachAlert.isShowing()) {
                        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
                    }
                    super.dismissInternal();
                }

                @Override
                public void onDismissAnimationStart() {
                    if (chatAttachAlert != null) {
                        chatAttachAlert.setFocusable(false);
                    }
                    if (chatAttachAlert != null && chatAttachAlert.isShowing()) {
                        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
                    }
                }
            };
            chatAttachAlert.setDelegate((button, arg, notify, scheduleDate, scheduleRepeatPeriod, effectId, invertMedia, forceDocument, payStars) -> {
                if (getParentActivity() == null || chatAttachAlert == null) {
                    return;
                }
                if (button == 8 || button == 7 || button == 4 && !chatAttachAlert.getPhotoLayout().getSelectedPhotos().isEmpty()) {
                    if (chatAttachAlert != null && button != 8) {
                        chatAttachAlert.dismiss(true);
                    }
                    assert chatAttachAlert != null;
                    final HashMap<Object, Object> selectedPhotos = chatAttachAlert.getPhotoLayout().getSelectedPhotos();
                    final ArrayList<Object> selectedPhotosOrder = chatAttachAlert.getPhotoLayout().getSelectedPhotosOrder();
                    if (!selectedPhotos.isEmpty()) {
                        // Lấy ảnh đầu tiên (vì bạn set MaxSelectedPhotos là 1)
                        Object key = selectedPhotosOrder.get(0);
                        MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) selectedPhotos.get(key);

                        String path = null;
                        if (photoEntry != null) {
                            if (photoEntry.imagePath != null) {
                                path = photoEntry.imagePath;
                            } else if (photoEntry.path != null) {
                                path = photoEntry.path;
                            }
                        }

                        if (path != null) {
                            displaySelectedImage(path);
                        }
                    }
                } else if (chatAttachAlert != null) {
                    chatAttachAlert.dismissWithButtonClick(button);
                }
            });
        }
    }

    private Bitmap loadBitMapToDisplay(String path) {
        if (path == null) return null;
        return ImageLoader.loadBitmap(path, null, Constants.IMAGE_SCALE_MAX_SIZE, Constants.IMAGE_SCALE_MAX_SIZE, true);
    }

    private void displaySelectedImage(String path) {
        if (path == null || ivSelectedImage == null) return;

        Bitmap bitmap = loadBitMapToDisplay(path);

        if (bitmap != null) {
            ivSelectedImage.setImageBitmap(bitmap);
            currentPicturePath = path;
            cvAction.setCardBackgroundColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            setChooseImageVisibility();
            setStateActionButton();
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getTitle());

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        createChatAttachView();
        requestPermissions(context);

        fragmentView = LayoutInflater.from(context).inflate(R.layout.ai_image_helper_layout, null);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        llPickImage = fragmentView.findViewById(R.id.llPickImage);
        llPickImage.setOnClickListener(v -> openAttachMenu());
        ivSelectedImage = fragmentView.findViewById(R.id.ivSelectedImage);
        TextView tvSelectedImage = fragmentView.findViewById(R.id.tvPickImage);
        tvSelectedImage.setTextColor(Theme.getColor(Theme.keyAiFeatureImageHelperPickImageText));


        cvAction = fragmentView.findViewById(R.id.cvAction);
        cvAction.setCardBackgroundColor(Theme.getColor(Theme.keyAiFeatureImageHelperActionBackground));
        cvAction.setOnClickListener(v -> handleAction());

        tvAction = fragmentView.findViewById(R.id.tvAction);
        tvAction.setText(getTitle());
        tvAction.setTextColor(Theme.getColor(Theme.keyAiFeatureImageHelperActionText));

        llAction = fragmentView.findViewById(R.id.llAction);

        CardView cvSave = fragmentView.findViewById(R.id.cvSave);
        cvSave.setOnClickListener(v -> {
            if (cacheFile == null) return;
            MediaController.saveFile(cacheFile.getAbsolutePath(), getContext(), 0, null, null);
            Toast.makeText(getParentActivity(), LocaleController.getString(R.string.ImageSaved), Toast.LENGTH_SHORT).show();
        });

        CardView cvCopy = fragmentView.findViewById(R.id.cvCopy);
        cvCopy.setOnClickListener(v -> {
            if (cacheFile == null) return;

            try {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                Uri uri = androidx.core.content.FileProvider.getUriForFile(getContext(),
                        ApplicationLoader.getApplicationId() + ".provider", cacheFile);

                ClipData clip = ClipData.newUri(getContext().getContentResolver(), "Image", uri);

                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getParentActivity(), LocaleController.getString(R.string.ImageCopied), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ignored) {
            }
            Toast.makeText(getParentActivity(), LocaleController.getString(R.string.ImageCopied), Toast.LENGTH_SHORT).show();
        });

        CardView cvShare = fragmentView.findViewById(R.id.cvShare);
        cvShare.setOnClickListener(v -> {
            if (cacheFile == null) return;

            Uri uri = FileProvider.getUriForFile(getContext(),
                    ApplicationLoader.getApplicationId() + ".provider", cacheFile);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            getParentActivity().startActivity(Intent.createChooser(share, LocaleController.getString(R.string.ShareFile)));
        });

        setStateActionButton();

        setChooseImageVisibility();

        setResultVisibility();

        return fragmentView;
    }

    private void handleAction() {
        if (getContext() instanceof androidx.fragment.app.FragmentActivity) {
            androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) getContext();
            loadingDialog.show(activity.getSupportFragmentManager(), LoadingDialog.TAG);
        }
        final File file = new File(currentPicturePath);
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));

        final MultipartBody.Part request = MultipartBody.Part.createFormData("image_file", file.getName(), requestFile);

        if (type == Constants.AIFeatureId.REMOVE_TEXT.id) {
            removeText(request);
        } else if (type == Constants.AIFeatureId.REMOVE_BACKGROUND.id) {
            removeBackground(request);
        } else if (type == Constants.AIFeatureId.UPSCALE.id) {
            upscaleImage(request);
        }
    }

    private void upscaleImage(MultipartBody.Part request) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPicturePath, options);

        RequestBody targetWidth = RequestBody.create(String.valueOf(options.outWidth * 2), MediaType.parse("text/plain"));
        RequestBody targetHeight = RequestBody.create(String.valueOf(options.outHeight * 2), MediaType.parse("text/plain"));

        imageRepository.upscaleImage(request, targetWidth, targetHeight, new IOnApiCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                loadingDialog.dismiss();
                if (data != null && data.length > 0) {
                    cacheFile = new File(AndroidUtilities.getCacheDir(), "ai_upscale_image" + System.currentTimeMillis() + ".png");
                    try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                        fos.write(data);
                        AndroidUtilities.runOnUIThread(() -> {
                            android.graphics.Bitmap bitmap = loadBitMapToDisplay(cacheFile.getAbsolutePath());
                            ivSelectedImage.setImageBitmap(bitmap);
                            setResultVisibility();
                        });
                    } catch (IOException ignored) {
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                loadingDialog.dismiss();
            }
        });
    }

    private void removeBackground(MultipartBody.Part request) {
        imageRepository.removeBackground(request, new IOnApiCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                loadingDialog.dismiss();
                if (data != null && data.length > 0) {
                    cacheFile = new File(AndroidUtilities.getCacheDir(), "ai_image_remove_background" + System.currentTimeMillis() + ".png");
                    try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                        fos.write(data);
                        AndroidUtilities.runOnUIThread(() -> {
                            if (type == Constants.AIFeatureId.REMOVE_BACKGROUND.id) {
                                ivSelectedImage.setBackground(new CheckerboardDrawable());
                            }
                            android.graphics.Bitmap bitmap = loadBitMapToDisplay(cacheFile.getAbsolutePath());
                            ivSelectedImage.setImageBitmap(bitmap);
                            setResultVisibility();
                        });
                    } catch (IOException ignored) {
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                loadingDialog.dismiss();
            }
        });
    }

    private void removeText(MultipartBody.Part request) {
        imageRepository.removeText(request, new IOnApiCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                loadingDialog.dismiss();
                if (data != null && data.length > 0) {
                    cacheFile = new File(AndroidUtilities.getCacheDir(), "ai_image_remove_text" + System.currentTimeMillis() + ".png");
                    try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                        fos.write(data);
                        AndroidUtilities.runOnUIThread(() -> {
                            android.graphics.Bitmap bitmap = loadBitMapToDisplay(cacheFile.getAbsolutePath());
                            ivSelectedImage.setImageBitmap(bitmap);
                            setResultVisibility();
                        });
                    } catch (IOException ignored) {
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                loadingDialog.dismiss();
            }
        });
    }

    private void openAttachMenu() {
        if (getParentActivity() == null) {
            return;
        }
        chatAttachAlert.getPhotoLayout().loadGalleryPhotos();
        chatAttachAlert.setMaxSelectedPhotos(1, true);
        chatAttachAlert.enableDefaultMode();
        chatAttachAlert.init();
        showDialog(chatAttachAlert);
    }

    private void setStateActionButton() {
        if (currentPicturePath != null) {
            cvAction.setClickable(true);
            cvAction.setAlpha(1f);
            tvAction.setAlpha(1f);
        } else {
            cvAction.setClickable(false);
            cvAction.setAlpha(0.5f);
            tvAction.setAlpha(0.5f);
        }
    }

    private void setChooseImageVisibility() {
        if (currentPicturePath != null) {
            llPickImage.setVisibility(View.GONE);
            ivSelectedImage.setVisibility(View.VISIBLE);
        } else {
            llPickImage.setVisibility(View.VISIBLE);
            ivSelectedImage.setVisibility(View.GONE);
        }
    }

    private void setResultVisibility() {
        if (cacheFile != null) {
            cvAction.setVisibility(View.GONE);
            llAction.setVisibility(View.VISIBLE);
        } else {
            cvAction.setVisibility(View.VISIBLE);
            llAction.setVisibility(View.GONE);
        }
    }

    private String getTitle() {
        if (type == Constants.AIFeatureId.REMOVE_TEXT.id) {
            return LocaleController.getString(R.string.remove_text);
        } else if (type == Constants.AIFeatureId.REMOVE_BACKGROUND.id) {
            return LocaleController.getString(R.string.remove_background);
        } else if (type == Constants.AIFeatureId.UPSCALE.id) {
            return LocaleController.getString(R.string.upscale);
        }
        return "";
    }

    @Override
    public void didSelectFiles(ArrayList<String> files, String caption, ArrayList<TLRPC.MessageEntity> captionEntities, ArrayList<MessageObject> fmessages, boolean notify, int scheduleDate, int scheduleRepeatPeriod, long effectId, boolean invertMedia, long payStars) {
        if (!files.isEmpty()) {
            String path = files.get(0);
            displaySelectedImage(path);
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 17 || requestCode == 18 && chatAttachAlert != null) {
            chatAttachAlert.getPhotoLayout().checkCamera(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            chatAttachAlert.getPhotoLayout().checkStorage();
        }
    }
}
