package de.symeda.sormas.app.task.edit;

import android.view.View;

import de.symeda.sormas.api.task.TaskStatus;
import de.symeda.sormas.app.BaseEditFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.contact.Contact;
import de.symeda.sormas.app.backend.event.Event;
import de.symeda.sormas.app.backend.task.Task;
import de.symeda.sormas.app.caze.read.CaseReadActivity;
import de.symeda.sormas.app.contact.read.ContactReadActivity;
import de.symeda.sormas.app.databinding.FragmentTaskEditLayoutBinding;
import de.symeda.sormas.app.event.read.EventReadActivity;

import static android.view.View.GONE;

public class TaskEditFragment extends BaseEditFragment<FragmentTaskEditLayoutBinding, Task, Task> {

    private Task record;

    public static TaskEditFragment newInstance(Task activityRootData) {
        return newInstance(TaskEditFragment.class, null, activityRootData);
    }

    private void setUpControlListeners(FragmentTaskEditLayoutBinding contentBinding) {
        contentBinding.setDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record.setTaskStatus(TaskStatus.DONE);
                getBaseEditActivity().saveData();
            }
        });

        contentBinding.setNotExecutable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record.setTaskStatus(TaskStatus.NOT_EXECUTABLE);
                getBaseEditActivity().saveData();
            }
        });

        if (record.getCaze() != null) {
            contentBinding.taskCaze.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Case caze = record.getCaze();
                    if (caze != null) {
                        CaseReadActivity.startActivity(getActivity(), caze.getUuid(), true);
                    }
                }
            });
        }

        if (record.getContact() != null) {
            contentBinding.taskContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Contact contact = record.getContact();
                    if (contact != null) {
                        ContactReadActivity.startActivity(getActivity(), contact.getUuid(), true);
                    }
                }
            });
        }

        if (record.getEvent() != null) {
            contentBinding.taskEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Event event = record.getEvent();
                    if (event != null) {
                        EventReadActivity.startActivity(getActivity(), event.getUuid(), true);
                    }
                }
            });
        }
    }

    // Overrides

    @Override
    protected String getSubHeadingTitle() {
        return getResources().getString(R.string.caption_task_information);
    }

    @Override
    public Task getPrimaryData() {
        return record;
    }

    @Override
    public boolean isShowSaveAction() {
        return false;
    }

    @Override
    protected void prepareFragmentData() {
        record = getActivityRootData();
    }

    @Override
    public void onLayoutBinding(FragmentTaskEditLayoutBinding contentBinding) {
        setUpControlListeners(contentBinding);

        // Saving and editing the assignee reply is only allowed when the task is assigned to the user;
        // Additionally, the save option is hidden for pending tasks because those should be saved
        // by clicking on the "Done" and "Not executable" buttons
        if (!ConfigProvider.getUser().equals(record.getAssigneeUser())) {
            contentBinding.taskAssigneeReply.setEnabled(false);
            contentBinding.taskButtonPanel.setVisibility(GONE);
        } else {
            if (record.getTaskStatus() != TaskStatus.PENDING) {
                getBaseEditActivity().getSaveMenu().setVisible(true);
                contentBinding.taskButtonPanel.setVisibility(GONE);
            }
        }

        contentBinding.setData(record);
    }

    @Override
    public int getEditLayout() {
        return R.layout.fragment_task_edit_layout;
    }

}
