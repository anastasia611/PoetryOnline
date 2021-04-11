<script>
    export let title = 'Remove icon';
    export let size = 8;
</script>

<!--TODO: move to button, not unique id-->
<label>
    <button on:click>+</button>
</label>

<style lang="scss">
    button {
        width: 1.5rem;
        height: 1.5rem;
        line-height: 1rem;

        margin-left: 0.75rem;
        padding: 0;

        border-radius: 1.5rem;
        border: none;

        font-size: 1.25rem;
        font-weight: 900;

        color: #500808;
        opacity: 0.2;

        &:hover, &:focus {
            background: none;
            opacity: 0.75;
        }
    }
</style>
