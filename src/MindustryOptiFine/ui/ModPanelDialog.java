package MindustryOptiFine.ui;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Http.*;
import arc.util.io.*;
import arc.util.serialization.*;
import mindustry.ctype.*;
import mindustry.core.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import MindustryOptiFine.shaders.ModShaders;
import MindustryOptiFine.graphics.ShaderCache;

import java.lang.reflect.*;
import java.util.*;

import static mindustry.Vars.*;

public class ModPanelDialog extends ModsDialog{
    protected float panelHeight = 110f;
    protected float panelWidth = 520f;

    public ModPanelDialog(){
        super();
        buttons.clear();
        buttons.button("@mods.guide", Icon.link, () -> Core.app.openURI(modGuideURL)).size(210, 64f);

        if(!mobile){
            buttons.button("@mods.openfolder", Icon.link, () -> Core.app.openFolder(modDirectory.absolutePath()));
        }
        
        addCloseButton();

        shown(() -> {
            customSetup();
            refreshModUpdates();
        });
        onResize(this::customSetup);

        Events.on(ResizeEvent.class, event -> {
            if(currentContent != null){
                currentContent.hide();
                currentContent = null;
            }
        });

        hidden(() -> {
            if(mods.requiresReload()){
                mods.reload();
            }

            if(updaterElement != null){
                updaterElement.remove();
                updaterElement = null;
            }
        });

        browser = new ModBrowserDialog();
    }

    void customSetup(){
        panelHeight = 110f;
        panelWidth = Math.min(Core.graphics.getWidth() / Scl.scl(1.05f) - Scl.scl(28f), 520f);

        cont.clear();
        cont.defaults().width(Math.min(Core.graphics.getWidth() / Scl.scl(1.05f), 556f)).pad(4);
        cont.add("@mod.reloadrequired").visible(mods::requiresReload).center().get().setAlignment(Align.center);
        cont.row();

        cont.table(buttons -> {
            buttons.left().defaults().growX().height(60f).uniformX();

            TextButtonStyle style = Styles.flatBordert;
            float margin = 12f;

            buttons.button("@mod.import", Icon.add, style, () -> {
                BaseDialog dialog = new BaseDialog("@mod.import");
                TextButtonStyle bstyle = Styles.flatt;

                dialog.cont.table(Tex.button, t -> {
                    t.defaults().size(300f, 70f);
                    t.margin(12f);

                    t.button("@mod.import.file", Icon.file, bstyle, () -> {
                        dialog.hide();
                        FileChooser.open("zip", "jar").submitMulti(files -> {
                            for(var file : files){
                                try{
                                    mods.importMod(file);
                                }catch(Exception e){
                                    ui.showException(e.getMessage() != null && e.getMessage().toLowerCase(Locale.ROOT).contains("writable dex") ? "@error.moddex" : "", e);
                                    Log.err(e);
                                }
                            }
                            customSetup();
                        });
                    }).margin(12f);

                    t.row();

                    t.button("@mod.import.github", Icon.github, bstyle, () -> {
                        dialog.hide();
                        ui.showTextInput("@mod.import.github", "", 64, Core.settings.getString("lastmod", ""), text -> {
                            text = text.trim().replace(" ", "");
                            if(text.startsWith("https://github.com/")) text = text.substring("https://github.com/".length());
                            Core.settings.put("lastmod", text);
                            githubImportMod(text, false, null, true);
                        });
                    }).margin(12f);
                });
                dialog.addCloseButton();
                dialog.show();
            }).margin(margin);

            buttons.button("@mods.browser", Icon.menu, style, () -> browser.show()).margin(margin);
        }).width(panelWidth);

        cont.row();

        if(!mods.list().isEmpty()){
            boolean[] anyDisabled = {false};
            Table[] pane = {null};

            Cons<String> rebuild = query -> {
                var cont = pane[0];
                cont.clear();
                boolean any = false;
                for(LoadedMod mod : mods.list()){
                    if(Strings.matches(query, mod.meta.displayName)){
                        any = true;
                        if(!mod.enabled() && !anyDisabled[0] && mods.list().size > 0){
                            anyDisabled[0] = true;
                            cont.row();
                            cont.image().growX().height(4f).pad(6f).color(Pal.gray).row();
                        }

                        boolean isOptiFineMod = mod.meta.name.equals("mindustry-optifine");

                        if(isOptiFineMod){
                            ModPanel panel = new ModPanel();
                            panel.top().left();
                            panel.margin(12f);

                            String stateDetails = getStateDetails(mod);
                            if(stateDetails != null){
                                panel.addListener(new Tooltip(f -> f.background(Styles.black8).margin(4f).add(stateDetails).growX().width(400f).wrap()));
                            }

                            panel.defaults().left().top();
                            panel.table(title1 -> {
                                title1.left();

                                title1.add(new BorderImage(){{
                                    if(mod.iconTexture != null){
                                        setDrawable(new TextureRegion(mod.iconTexture));
                                    }else{
                                        setDrawable(Tex.nomap);
                                    }
                                    border(Pal.accent);
                                }}).size(panelHeight - 8f).padTop(-8f).padLeft(-8f).padRight(8f);

                                title1.table(text -> {
                                    boolean hideDisabled = !mod.isSupported() || mod.hasUnmetDependencies() || mod.hasContentErrors();
                                    String shortDesc = mod.meta.shortDescription();

                                    text.add("[accent]" + Strings.stripColors(mod.meta.displayName) + "\n" +
                                        (shortDesc.length() > 0 ? "[lightgray]" + shortDesc + "\n" : "")
                                        + (mod.enabled() || hideDisabled ? "" : Core.bundle.get("mod.disabled") + ""))
                                    .wrap().top().width(300f).growX().left();

                                    text.row();

                                    String state = getStateText(mod);
                                    if(state != null){
                                        text.labelWrap(state).growX().row();
                                    }
                                }).top().growX();

                                title1.add().growX();
                            }).growX().growY().left();

                            panel.table(right -> {
                                right.right();
                                right.button(mod.enabled() ? Icon.downOpen : Icon.upOpen, Styles.clearNonei, () -> {
                                    mods.setEnabled(mod, !mod.enabled());
                                    customSetup();
                                }).size(50f).disabled(!mod.isSupported());

                                right.button(mod.hasSteamID() ? Icon.link : Icon.trash, Styles.clearNonei, () -> {
                                    if(!mod.hasSteamID()){
                                        ui.showConfirm("@confirm", "@mod.remove.confirm", () -> {
                                            mods.removeMod(mod);
                                            withUpdates.remove(mod);
                                            customSetup();
                                        });
                                    }else{
                                        platform.viewListing(mod);
                                    }
                                }).size(50f);

                                if(steam && !mod.hasSteamID()){
                                    right.row();
                                    right.button(Icon.export, Styles.clearNonei, () -> {
                                        platform.publish(mod);
                                    }).size(50f);
                                }
                            }).growX().right().padRight(-8f).padTop(-8f);

                            cont.add(panel).size(panelWidth, panelHeight).growX().pad(4f).padTop(8f).row();
                        }else{
                            cont.button(t -> {
                                t.top().left();
                                t.margin(12f);

                                String stateDetails = getStateDetails(mod);
                                if(stateDetails != null){
                                    t.addListener(new Tooltip(f -> f.background(Styles.black8).margin(4f).add(stateDetails).growX().width(400f).wrap()));
                                }

                                t.defaults().left().top();
                                t.table(title1 -> {
                                    title1.left();

                                    title1.add(new BorderImage(){{
                                        if(mod.iconTexture != null){
                                            setDrawable(new TextureRegion(mod.iconTexture));
                                        }else{
                                            setDrawable(Tex.nomap);
                                        }
                                        border(Pal.accent);
                                    }}).size(panelHeight - 8f).padTop(-8f).padLeft(-8f).padRight(8f);

                                    title1.table(text -> {
                                        boolean hideDisabled = !mod.isSupported() || mod.hasUnmetDependencies() || mod.hasContentErrors();
                                        String shortDesc = mod.meta.shortDescription();

                                        text.add("[accent]" + Strings.stripColors(mod.meta.displayName) + "\n" +
                                            (shortDesc.length() > 0 ? "[lightgray]" + shortDesc + "\n" : "")
                                            + (mod.enabled() || hideDisabled ? "" : Core.bundle.get("mod.disabled") + ""))
                                        .wrap().top().width(300f).growX().left();

                                        text.row();

                                        String state = getStateText(mod);
                                        if(state != null){
                                            text.labelWrap(state).growX().row();
                                        }
                                    }).top().growX();

                                    title1.add().growX();
                                }).growX().growY().left();

                                t.table(right -> {
                                    right.right();
                                    right.button(mod.enabled() ? Icon.downOpen : Icon.upOpen, Styles.clearNonei, () -> {
                                        mods.setEnabled(mod, !mod.enabled());
                                        customSetup();
                                    }).size(50f).disabled(!mod.isSupported());

                                    right.button(mod.hasSteamID() ? Icon.link : Icon.trash, Styles.clearNonei, () -> {
                                        if(!mod.hasSteamID()){
                                            ui.showConfirm("@confirm", "@mod.remove.confirm", () -> {
                                                mods.removeMod(mod);
                                                withUpdates.remove(mod);
                                                customSetup();
                                            });
                                        }else{
                                            platform.viewListing(mod);
                                        }
                                    }).size(50f);

                                    if(steam && !mod.hasSteamID()){
                                        right.row();
                                        right.button(Icon.export, Styles.clearNonei, () -> {
                                            platform.publish(mod);
                                        }).size(50f);
                                    }
                                }).growX().right().padRight(-8f).padTop(-8f);
                            }, Styles.grayt, () -> showMod(mod)).size(panelWidth, panelHeight).growX().pad(4f).padTop(8f).row();
                        }

                        if(hasUpdate(mod)){
                            cont.button(b -> {
                                b.margin(6f);
                                b.left();
                                b.image(Icon.download).color(Color.lightGray).size(iconMed).padRight(8f);
                                var list = modToListing.get(mod);
                                b.add(Core.bundle.format("mods.update.available", list == null ? "<unknown>" : list.version));
                            }, Styles.grayt, () -> {
                                githubImportMod(mod.getRepo(), mod.isJava(), null, false);
                            }).width(panelWidth).height(48f).padTop(-4f).row();
                        }
                    }
                }

                if(!any){
                    cont.add("@none.found").color(Color.lightGray).pad(4);
                }
            };

            if(!mobile || Core.graphics.isPortrait()){
                cont.table(search -> {
                    search.image(Icon.zoom).padRight(8f);
                    search.field("", rebuild).growX();
                }).fillX().padBottom(4);
            }

            cont.row();
            cont.pane(table1 -> {
                pane[0] = table1.margin(10f).top();
                rebuild.get("");
            }).scrollX(false).update(s -> scroll = s.getScrollY()).get().setScrollYForce(scroll);

            cont.row();

            if(withUpdates.size > 1){
                cont.button(b -> {
                    b.margin(6f);
                    b.image(Icon.download).size(iconMed).padRight(8f);
                    b.add("@mods.update.all");
                    b.image(Icon.download).size(iconMed).padLeft(8f);
                }, Styles.grayt, () -> {
                    var queue = withUpdates.toSeq();
                    int[] index = {0};
                    cancelledImport = false;
                    if(updaterElement != null) updaterElement.remove();
                    updaterElement = new Element();

                    updaterElement.update(() -> {
                        if(index[0] >= queue.size || cancelledImport){
                            updaterElement.remove();
                        }else if(!ui.loadfrag.shown()){
                            var next = queue.get(index[0] ++);
                            githubImportMod(next.getRepo(), next.isJava(), null, false);
                        }
                    });
                    addChild(updaterElement);
                }).padTop(8f).width(panelWidth).height(60f).padTop(12f).row();
            }
        }else{
            cont.table(Styles.black6, t -> t.add("@mods.none")).height(80f);
        }

        cont.row();
    }

    private @Nullable String getStateText(LoadedMod item){
        if(item.isOutdated()){
            return "@mod.incompatiblemod";
        }else if(item.isBlacklisted()){
            return "@mod.blacklisted";
        }else if(!item.isSupported()){
            return "@mod.incompatiblegame";
        }else if(item.state == ModState.circularDependencies){
            return "@mod.circulardependencies";
        }else if(item.state == ModState.incompleteDependencies){
            return "@mod.incompletedependencies";
        }else if(item.hasUnmetDependencies()){
            return "@mod.unmetdependencies";
        }else if(item.hasContentErrors()){
            return "@mod.erroredcontent";
        }else if(item.meta.hidden){
            return "@mod.multiplayer.compatible";
        }
        return null;
    }

    protected void showMod(LoadedMod mod){
        BaseDialog dialog = new BaseDialog(mod.meta.displayName);

        dialog.addCloseButton();

        if(!mobile){
            dialog.buttons.button("@mods.openfolder", Icon.link, () -> Core.app.openFolder(mod.file.absolutePath()));
        }

        if(mod.getRepo() != null){
            boolean showImport = !mod.hasSteamID();
            dialog.buttons.button("@mods.github.open", Icon.link, () -> Core.app.openURI("https://github.com/" + mod.getRepo()));
            if(mobile && showImport) dialog.buttons.row();
            if(showImport) dialog.buttons.button("@mods.browser.reinstall", Icon.download, () -> githubImportMod(mod.getRepo(), mod.isJava(), null, false));
        }

        dialog.cont.pane(desc -> {
            desc.center();
            desc.defaults().padTop(10).left();

            desc.add("@editor.name").padRight(10).color(Color.gray).padTop(0);
            desc.row();
            desc.add(mod.meta.displayName).growX().wrap().padTop(2);
            desc.row();
            if(mod.meta.author != null){
                desc.add("@editor.author").padRight(10).color(Color.gray);
                desc.row();
                desc.add(mod.meta.author).growX().wrap().padTop(2);
                desc.row();
            }
            if(mod.meta.version != null){
                desc.add("@mod.version").padRight(10).color(Color.gray).top();
                desc.row();
                desc.add(mod.meta.version).growX().wrap().padTop(2);
                desc.row();
            }
            if(mod.meta.description != null){
                desc.add("@editor.description").padRight(10).color(Color.gray).top();
                desc.row();
                desc.add(mod.meta.description).growX().wrap().padTop(2);
                desc.row();
            }

            String state = getStateDetails(mod);

            if(state != null){
                desc.add("@mod.disabled").padTop(13f).padBottom(-6f).row();
                desc.add(state).growX().wrap().row();
            }

        }).width(400f);

        Seq<UnlockableContent> all = Seq.with(content.getContentMap()).<Content>flatten().select(c -> c.minfo.mod == mod && c instanceof UnlockableContent u && !u.isHidden()).as();
        if(all.any()){
            dialog.cont.row();
            dialog.cont.button("@mods.viewcontent", Icon.book, () -> {
                BaseDialog d = new BaseDialog(mod.meta.displayName);
                d.cont.pane(cs -> {
                    int i = 0;
                    for(UnlockableContent c : all){
                        cs.button(new TextureRegionDrawable(c.uiIcon), Styles.flati, iconMed, () -> {
                            ui.content.show(c);
                        }).size(50f).with(im -> {
                            var click = im.getClickListener();
                            im.update(() -> im.getImage().color.lerp(!click.isOver() ? Color.lightGray : Color.white, 0.4f * Time.delta));

                        }).tooltip(c.localizedName);

                        if(++i % (int)Math.min(Core.graphics.getWidth() / Scl.scl(110), 14) == 0) cs.row();
                    }
                }).grow();
                d.addCloseButton();
                d.show();
                currentContent = d;
            }).size(300, 50).pad(4);
        }

        dialog.show();
    }

    private @Nullable String getStateDetails(LoadedMod item){
        if(item.isOutdated()){
            return "@mod.incompatiblemod.details";
        }else if(item.isBlacklisted()){
            return "@mod.blacklisted.details";
        }else if(!item.isSupported()){
            return Core.bundle.format("mod.requiresversion.details", item.meta.minGameVersion);
        }else if(item.state == ModState.circularDependencies){
            return "@mod.circulardependencies.details";
        }else if(item.state == ModState.incompleteDependencies){
            return Core.bundle.format("mod.incompletedependencies.details", item.missingDependencies.toString(", "));
        }else if(item.hasUnmetDependencies()){
            return Core.bundle.format("mod.missingdependencies.details", item.missingDependencies.toString(", "));
        }else if(item.hasContentErrors()){
            return "@mod.erroredcontent.details";
        }
        return null;
    }

    public static void replaceModsDialog(){
        try{
            Field modsField = UI.class.getDeclaredField("mods");
            modsField.setAccessible(true);
            
            Object currentValue = modsField.get(ui);
            Log.info("Current mods dialog type: " + (currentValue != null ? currentValue.getClass().getName() : "null"));
            
            ModPanelDialog dialog = new ModPanelDialog();
            modsField.set(ui, dialog);
            
            Object newValue = modsField.get(ui);
            Log.info("New mods dialog type: " + (newValue != null ? newValue.getClass().getName() : "null"));
            Log.info("ModsDialog field replacement successful!");
            
            try{
                Field menufragField = UI.class.getDeclaredField("menufrag");
                menufragField.setAccessible(true);
                Object menufrag = menufragField.get(ui);
                
                Field buttonsField = menufrag.getClass().getDeclaredField("buttons");
                buttonsField.setAccessible(true);
                Object buttons = buttonsField.get(menufrag);
                
                if(buttons instanceof Seq){
                    Seq<?> buttonList = (Seq<?>)buttons;
                    for(Object obj : buttonList){
                        if(obj != null && obj.getClass().getSimpleName().equals("MenuButton")){
                            try{
                                Field textField = obj.getClass().getDeclaredField("text");
                                textField.setAccessible(true);
                                String text = (String)textField.get(obj);
                                
                                if("@mods".equals(text)){
                                    Field runnableField = obj.getClass().getDeclaredField("runnable");
                                    runnableField.setAccessible(true);
                                    runnableField.set(obj, (Runnable)ui.mods::show);
                                    Log.info("Successfully replaced mods button runnable!");
                                }
                            }catch(Exception ex){
                                Log.err("Failed to modify menu button: " + ex.getMessage());
                            }
                        }
                    }
                }
            }catch(Exception ex){
                Log.err("Failed to modify menu buttons: " + ex.getMessage());
            }
            
        }catch(NoSuchFieldException e){
            Log.err("Failed to find 'mods' field in UI class", e);
        }catch(IllegalAccessException e){
            Log.err("Failed to access 'mods' field in UI class", e);
        }catch(Exception e){
            Log.err("Unexpected error replacing ModsDialog", e);
        }
    }
}