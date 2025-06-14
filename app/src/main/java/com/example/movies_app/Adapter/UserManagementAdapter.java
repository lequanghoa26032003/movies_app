package com.example.movies_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Database.entity.User;
import com.example.movies_app.R;
import com.example.movies_app.service.UserManagementService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private UserActionListener listener;
    private Set<Integer> selectedUserIds = new HashSet<>();

    public interface UserActionListener {
        void onEditUser(User user);
        void onDeleteUser(User user);
        void onToggleUserStatus(User user);
        void onViewUserDetails(User user);
        void onUserSelected(User user, boolean isSelected);
    }

    public UserManagementAdapter(Context context, List<User> userList, UserActionListener listener) {
        this.context = context;
        this.userList = userList;
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
        User user = userList.get(position);

        // Bind basic user info
        holder.usernameText.setText(user.getUsername());
        holder.emailText.setText(user.getEmail());

        // Set role with background color
        holder.roleText.setText(user.getRole());
        if ("ADMIN".equals(user.getRole())) {
            holder.roleText.setBackgroundColor(ContextCompat.getColor(context, R.color.purple));
        } else {
            holder.roleText.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
        }

        // Set status with background color
        String statusText = UserManagementService.getStatusName(user.getAccountStatus());
        holder.statusText.setText(statusText);

        switch (user.getAccountStatus()) {
            case UserManagementService.STATUS_ACTIVE:
                holder.statusText.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                break;
            case UserManagementService.STATUS_BLOCKED:
                holder.statusText.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                break;
            default:
                holder.statusText.setBackgroundColor(ContextCompat.getColor(context, R.color.orange));
                break;
        }

        // Set checkbox state
        holder.selectCheckBox.setOnCheckedChangeListener(null); // Remove listener temporarily
        holder.selectCheckBox.setChecked(selectedUserIds.contains(user.getUserId()));
        holder.selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUserIds.add(user.getUserId());
            } else {
                selectedUserIds.remove(user.getUserId());
            }
            if (listener != null) {
                listener.onUserSelected(user, isChecked);
            }
        });

        // Set toggle status button icon
        if (user.getAccountStatus() == UserManagementService.STATUS_BLOCKED) {
            holder.toggleStatusButton.setImageResource(R.drawable.ic_lock);
            holder.toggleStatusButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.orange));
        } else {
            holder.toggleStatusButton.setImageResource(R.drawable.ic_unlock);
            holder.toggleStatusButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green));
        }

        // Set click listeners
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) listener.onEditUser(user);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteUser(user);
        });

        holder.toggleStatusButton.setOnClickListener(v -> {
            if (listener != null) listener.onToggleUserStatus(user);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onViewUserDetails(user);
        });

        // Long click to toggle selection
        holder.itemView.setOnLongClickListener(v -> {
            holder.selectCheckBox.setChecked(!holder.selectCheckBox.isChecked());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void clearSelection() {
        selectedUserIds.clear();
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, emailText, roleText, statusText;
        CheckBox selectCheckBox;
        ImageButton editButton, deleteButton, toggleStatusButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            emailText = itemView.findViewById(R.id.emailText);
            roleText = itemView.findViewById(R.id.roleText);
            statusText = itemView.findViewById(R.id.statusText);
            selectCheckBox = itemView.findViewById(R.id.selectCheckBox);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            toggleStatusButton = itemView.findViewById(R.id.toggleStatusButton);
        }
    }
}