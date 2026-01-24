package net.md_5.bungee.tab;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;

public class ServerUnique extends TabList
{

    private final Collection<UUID> uuids = new HashSet<>();
    private final Collection<String> usernames = new HashSet<>();

    public ServerUnique(ProxiedPlayer player)
    {
        super( player );
    }

    @Override
    public void onUpdate(PlayerListItem playerListItem)
    {
        PlayerListItem.Action action = playerListItem.getAction();

        if ( action == PlayerListItem.Action.ADD_PLAYER )
        {
            for ( PlayerListItem.Item item : playerListItem.getItems() )
            {
                if ( item.getUuid() != null )
                {
                    uuids.add( item.getUuid() );
                } else
                {
                    usernames.add( item.getUsername() );
                }
            }
        } else if ( action == PlayerListItem.Action.REMOVE_PLAYER )
        {
            for ( PlayerListItem.Item item : playerListItem.getItems() )
            {
                if ( item.getUuid() != null )
                {
                    uuids.remove( item.getUuid() );
                } else
                {
                    usernames.remove( item.getUsername() );
                }
            }
        }
        player.unsafe().sendPacket( playerListItem );
    }

    @Override
    public void onUpdate(PlayerListItemRemove playerListItem)
    {
        for ( UUID uuid : playerListItem.getUuids() )
        {
            uuids.remove( uuid );
        }
        player.unsafe().sendPacket( playerListItem );
    }

    @Override
    public void onUpdate(PlayerListItemUpdate playerListItem)
    {
        if ( playerListItem.getActions().contains( PlayerListItemUpdate.Action.ADD_PLAYER ) )
        {
            for ( PlayerListItem.Item item : playerListItem.getItems() )
            {
                uuids.add( item.getUuid() );
            }
        }
        player.unsafe().sendPacket( playerListItem );
    }

    @Override
    public void onPingChange(int ping)
    {

    }

    @Override
    public void onServerChange()
    {
        PlayerListItem lPacket = new PlayerListItem();
        lPacket.setAction( PlayerListItem.Action.REMOVE_PLAYER );
        PlayerListItem.Item[] lItems = new PlayerListItem.Item[ uuids.size() + usernames.size() ];
        int l = 0;
        for ( UUID uuid : uuids )
        {
            PlayerListItem.Item item = lItems[l++] = new PlayerListItem.Item();
            item.setUuid( uuid );
        }
        for ( String username : usernames )
        {
            PlayerListItem.Item item = lItems[l++] = new PlayerListItem.Item();
            item.setUsername( username );
            item.setDisplayName( TextComponent.fromLegacy( username ) );
        }
        lPacket.setItems( lItems );

        if ( player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_19_3 )
        {
            PlayerListItemRemove packet = new PlayerListItemRemove();
            packet.setUuids( uuids.toArray( new UUID[ 0 ] ) );
            player.unsafe().sendPacket( packet );
        } else if ( player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_8 )
        {
            player.unsafe().sendPacket( lPacket );
        } else
        {
            for ( PlayerListItem.Item item : lPacket.getItems() )
            {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction( lPacket.getAction() );

                packet.setItems( new PlayerListItem.Item[] {
                    item
                } );
                player.unsafe().sendPacket( packet );
            }
        }
        uuids.clear();
        usernames.clear();
    }

    @Override
    public void onConnect()
    {

    }

    @Override
    public void onDisconnect()
    {

    }

}
