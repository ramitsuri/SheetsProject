package com.ramitsuri.project.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.ramitsuri.expensemanager.Constants;
import com.ramitsuri.expensemanager.IntDefs.ListItemType;
import com.ramitsuri.expensemanager.R;
import com.ramitsuri.expensemanager.entities.Expense;
import com.ramitsuri.expensemanager.entities.ExpenseWrapper;
import com.ramitsuri.expensemanager.ui.adapter.ExpenseAdapter;
import com.ramitsuri.expensemanager.utils.AppHelper;
import com.ramitsuri.expensemanager.utils.CurrencyHelper;
import com.ramitsuri.expensemanager.utils.DialogHelper;
import com.ramitsuri.expensemanager.utils.WorkHelper;
import com.ramitsuri.expensemanager.viewModel.ExpensesViewModel;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class ExpensesFragment extends BaseFragment {

    private ExpensesViewModel mExpensesViewModel;

    // Views
    private ExtendedFloatingActionButton mBtnAdd;
    private MaterialCardView mCardInfo;
    private TextView mTextInfoEmpty, mTextInfoEmptyHelp, mTextInfo1, mTextInfo2, mTextInfo3;
    private ImageView mImgDownArrow;

    public ExpensesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mExpensesViewModel = ViewModelProviders.of(this).get(ExpensesViewModel.class);

        mBtnAdd = view.findViewById(R.id.btn_add);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ExpensesFragment.this)
                        .navigate(R.id.nav_action_add_expense, null);
            }
        });

        // Shown when no expenses
        mTextInfoEmpty = view.findViewById(R.id.txt_expense_empty);
        mTextInfoEmptyHelp = view.findViewById(R.id.txt_expense_empty_help);
        mImgDownArrow = view.findViewById(R.id.img_down);

        // Shown when there are expenses
        mCardInfo = view.findViewById(R.id.card_info);
        mTextInfo1 = view.findViewById(R.id.txt_expense_info_1);
        mTextInfo2 = view.findViewById(R.id.txt_expense_info_2);
        mTextInfo3 = view.findViewById(R.id.txt_expense_info_3);

        Button btnSyncNow = view.findViewById(R.id.btn_sync_now);
        btnSyncNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountName = AppHelper.getAccountName();
                String accountType = AppHelper.getAccountType();
                if (accountName != null && accountType != null) {
                    initiateBackup();
                } else {
                    Timber.w("AccountType or Name null. Name " + accountName + ", Type " +
                            accountType);
                }
            }
        });

        //if (AppHelper.isAutoBackupEnabled()) { // Always hidden for now
        btnSyncNow.setVisibility(View.GONE);
        //}

        setupListExpenses(view);

        final MotionLayout motionContainer = view.findViewById(R.id.layout);
        setupAnimations(motionContainer);

        // Work Status
        logWorkStatus(WorkHelper.getOneTimeWorkTag());
        logWorkStatus(WorkHelper.getPeriodicWorkTag());
    }

    private void setupListExpenses(View view) {
        final RecyclerView listExpenses = view.findViewById(R.id.list_expenses);
        final int numberOfColumns = getResources().getInteger(R.integer.expenses_grid_columns);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), numberOfColumns);

        final ExpenseAdapter adapter = new ExpenseAdapter();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(
                    int position) { // Set headers to span the whole width of recycler view
                @ListItemType
                int itemType = adapter.getItemViewType(position);
                switch (itemType) {
                    case ListItemType.HEADER:
                        return numberOfColumns;

                    case ListItemType.ITEM:

                    default:
                        return 1; // Width should span only 1 column for items
                }
            }
        });
        listExpenses.setAdapter(adapter);
        listExpenses.setLayoutManager(manager);
        mExpensesViewModel.getExpenses().observe(this, new Observer<List<ExpenseWrapper>>() {
            @Override
            public void onChanged(List<ExpenseWrapper> expenses) {
                Timber.i("Refreshing expenses");
                adapter.setExpenses(expenses);
                setTextInfo(expenses);
            }
        });
        listExpenses.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    mBtnAdd.hide();
                } else if (dy < 0) {
                    mBtnAdd.show();
                }
            }
        });

        adapter.setCallback(new ExpenseAdapter.ItemClickListener() {
            @Override
            public void onItemClicked(@NonNull ExpenseWrapper wrapper) {
                showExpenseDetails(wrapper);
            }
        });
    }

    private void setupAnimations(final MotionLayout motionContainer) {
        motionContainer.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {

            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {

            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                if (i == R.id.ending_set) {
                    motionContainer.transitionToStart();
                }
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

            }
        });
        mImgDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motionContainer.transitionToEnd();
            }
        });
    }

    private void setTextInfo(List<ExpenseWrapper> expenses) {
        boolean doCalculation = false;
        if (expenses.size() == 0) {
            mTextInfoEmpty.setVisibility(View.VISIBLE);
            mTextInfoEmptyHelp.setVisibility(View.GONE);
            mCardInfo.setVisibility(View.GONE);
        } else {
            doCalculation = true;
            mTextInfoEmpty.setVisibility(View.GONE);
            mTextInfoEmptyHelp.setVisibility(View.GONE);
            mCardInfo.setVisibility(View.VISIBLE);
        }

        // Return when no calculation required (TextInfo 1, 2, 3 are not going to be shown)
        if (!doCalculation) {
            return;
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        int count = 0;
        int startDateIndex = -1;
        int endDateIndex = -1;
        for (int i = 0; i < expenses.size(); i++) {
            /* List is in descending order of date.
             * First header encountered is end date.
             * Last header encountered is the start date
             */
            ExpenseWrapper wrapper = expenses.get(i);
            if (wrapper.getItemType() == ListItemType.ITEM) {
                count = count + 1;
                totalAmount = totalAmount.add(expenses.get(i).getExpense().getAmount());
            } else if (wrapper.getItemType() == ListItemType.HEADER) {
                if (endDateIndex == -1) {
                    endDateIndex = i;
                }
                startDateIndex = i;
            }
        }
        // Number of expenses
        mTextInfo1.setText(getResources().getQuantityString(R.plurals.expense_count, count, count));

        // Date range
        if (startDateIndex == endDateIndex) {
            mTextInfo2.setText(String.format("(%1s)",
                    expenses.get(startDateIndex).getDate()));
        } else {
            mTextInfo2.setText(String.format("(%1s - %2s)",
                    expenses.get(startDateIndex).getDate(),
                    expenses.get(endDateIndex).getDate()));
        }

        // Total
        mTextInfo3.setText(CurrencyHelper.formatForDisplay(true, totalAmount));
    }

    private void showExpenseDetails(ExpenseWrapper wrapper) {
        Timber.i("Showing information for %s", wrapper.toString());
        ExpenseDetailsFragment detailsFragment = ExpenseDetailsFragment.newInstance();
        detailsFragment.setCallback(new ExpenseDetailsFragment.DetailFragmentCallback() {
            @Override
            public void onEditRequested(@NonNull Expense expense) {
                handleExpenseEditRequested(expense);
            }

            @Override
            public void onDeleteRequested(@NonNull Expense expense) {
                handleExpenseDeleteRequested(expense);
            }
        });
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BundleKeys.SELECTED_EXPENSE, wrapper.getExpense());
        detailsFragment.setArguments(bundle);
        if (getActivity() != null) {
            detailsFragment
                    .show(getActivity().getSupportFragmentManager(), ExpenseDetailsFragment.TAG);
        } else {
            Timber.e("getActivity() returned null when showing details fragment");
        }
    }

    private void handleExpenseEditRequested(@Nonnull Expense expense) {
        ExpensesFragmentDirections.NavActionAddExpense addAction
                = ExpensesFragmentDirections.navActionAddExpense();
        addAction.setExpense(expense);
        NavHostFragment.findNavController(this).navigate(addAction);
    }

    private void handleExpenseDeleteRequested(@Nonnull final Expense expense) {
        if (getContext() != null) {
            DialogInterface.OnClickListener positiveListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mExpensesViewModel.deleteExpense(expense);
                        }
                    };
            DialogHelper.showAlert(getContext(),
                    R.string.common_warning, R.string.delete_expense_warning_message,
                    R.string.common_yes, positiveListener,
                    R.string.common_no, null);
        }
    }

    private void initiateBackup() {
        Timber.i("Initiating backup");

        WorkHelper.enqueueOneTimeBackup();
    }
}
