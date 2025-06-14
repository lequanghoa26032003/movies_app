package com.example.movies_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.movies_app.Database.entity.User;
import com.example.movies_app.R;
import com.example.movies_app.service.UserManagementService;

import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {
    
    private Context context;
    private List<User> users;
    private UserActionListener listener;
    
    public interface UserActionListener {
        void onEditUser(User user);
        void onDeleteUser(User user);
        void onToggleUserStatus(User user);
        void onViewUserDetails(User user);
    }
    
    public UserManagementAdapter(Context context, List<User> users, UserActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_management, parent, false);
        return new UserViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    class UserViewHolder extends RecyclerView.ViewHolder {
        
        ImageView imageViewAvatar;
        TextView textViewUsername;
        TextView textViewEmail;
        TextView textViewFullName;
        TextView textViewRole;
        TextView textViewStatus;
        TextView textViewRegistrationDate;
        TextView textViewLastLogin;
        ImageButton buttonEdit;
        ImageButton buttonDelete;
        ImageButton buttonToggleStatus;
        View statusIndicator;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewFullName = itemView.findViewById(R.id.textViewFullName);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewRegistrationDate = itemView.findViewById(R.id.textViewRegistrationDate);
            textViewLastLogin = itemView.findViewById(R.id.textViewLastLogin);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonToggleStatus = itemView.findViewById(R.id.buttonToggleStatus);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            
            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewUserDetails(users.get(getAdapterPosition()));
                }
            });
            
            buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditUser(users.get(getAdapterPosition()));
                }
            });
            
            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUser(users.get(getAdapterPosition()));
                }
            });
            
            buttonToggleStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleUserStatus(users.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(User user) {
            // Set basic info
            textViewUsername.setText(user.getUsername());
            textViewEmail.setText(user.getEmail());
            textViewFullName.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật");
            textViewRole.setText(user.getRole());
            textViewRegistrationDate.setText("Đăng ký: " + user.getRegistrationDate());
            
            // Set last login
            String lastLogin = user.getLastLoginDate();
            textViewLastLogin.setText("Đăng nhập cuối: " + (lastLogin != null ? lastLogin : "Chưa đăng nhập"));
            
            // Set status
            setupStatus(user);
            
            // Set avatar
            setupAvatar(user);
            
            // Setup action buttons
            setupActionButtons(user);
        }
        
        private void setupStatus(User user) {
            int status = user.getAccountStatus();
            String statusText = UserManagementService.getStatusName(status);
            textViewStatus.setText(statusText);
            
            // Set status color
            int colorRes;
            switch (status) {
                case UserManagementService.STATUS_ACTIVE:
                    colorRes = R.color.status_active;
                    break;
                case UserManagementService.STATUS_BLOCKED:
                    colorRes = R.color.status_blocked;
                    break;
                case UserManagementService.STATUS_INACTIVE:
                    colorRes = R.color.status_inactive;
                    break;
                default:
                    colorRes = R.color.status_inactive;
                    break;
            }
            
            int color = ContextCompat.getColor(context, colorRes);
            textViewStatus.setTextColor(color);
            statusIndicator.setBackgroundColor(color);
        }
        
        private void setupAvatar(User user) {
            String avatarUrl = user.getAvatarUrl();
            
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .circleCrop();
            
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(context)
                        .load(avatarUrl)
                        .apply(options)
                        .into(imageViewAvatar);
            } else {
                // Tạo avatar mặc định với chữ cái đầu
                imageViewAvatar.setImageResource(R.drawable.ic_user_placeholder);
            }
        }
        
        private void setupActionButtons(User user) {
            // Toggle status button
            if (user.getAccountStatus() == UserManagementService.STATUS_BLOCKED) {
                buttonToggleStatus.setImageResource(R.drawable.ic_unlock);
                buttonToggleStatus.setContentDescription("Mở khóa");
            } else {
                buttonToggleStatus.setImageResource(R.drawable.ic_lock);
                buttonToggleStatus.setContentDescription("Khóa");
            }
            
            // Disable actions for current user (nếu cần)
            // Có thể thêm logic để ẩn nút delete/block cho chính user đang đăng nhập
        }
    }
}