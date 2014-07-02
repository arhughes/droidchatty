package cc.hughes.droidchatty2.net;

import cc.hughes.droidchatty2.R;

public enum TreeBullet {
    Blank(R.drawable.bullet_blank),
    End(R.drawable.bullet_end),
    EndNew(R.drawable.bullet_endnew),
    Branch(R.drawable.bullet_branch),
    BranchNew(R.drawable.bullet_branchnew),
    Extend(R.drawable.bullet_extendpast),
    ExtendNew(R.drawable.bullet_extendpastnew),
    Collapse(R.drawable.bullet_collapse),
    CollapseNew(R.drawable.bullet_collapse);

    private final int mResource;

    TreeBullet(int resource) {
        mResource = resource;
    }

    public int getResource() {
        return mResource;
    }
}